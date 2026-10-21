import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


DEPLOY_SCRIPT = Path(__file__).resolve().parents[1] / "deploy.sh"


class DeploymentTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        (self.root / "deployment").mkdir()
        self.schema = self.root / "deployment/schema.sql"
        self.schema.write_text("CREATE TABLE example (id INTEGER);\n")
        self.checksum = self.root / "deployment/.schema.sha256"
        self.log = self.root / "docker-calls.jsonl"
        self.sql = self.root / "executed.sql"
        binary_directory = self.root / "bin"
        binary_directory.mkdir()
        docker = binary_directory / "docker"
        docker.write_text('''#!/usr/bin/env python3
import json
import os
from pathlib import Path
import sys

args = sys.argv[1:]
with open(os.environ["DOCKER_CALL_LOG"], "a") as log:
    log.write(json.dumps(args) + "\\n")
if "exec" in args:
    Path(os.environ["SQL_CAPTURE"]).write_text(sys.stdin.read())
    if os.environ.get("FAIL_SQL") == "1":
        sys.exit(1)
if "pull" in args and os.environ.get("FAIL_PULL") == "1":
    sys.exit(1)
''')
        docker.chmod(0o755)
        self.environment = dict(os.environ)
        self.environment.update({
            "PATH": str(binary_directory) + os.pathsep + os.environ["PATH"],
            "DOCKER_CALL_LOG": str(self.log),
            "SQL_CAPTURE": str(self.sql),
        })

    def run_deploy(self, **environment):
        return subprocess.run(
            ["bash", str(DEPLOY_SCRIPT)], cwd=self.root,
            env={**self.environment, **environment}, capture_output=True, text=True,
        )

    def commands(self):
        return [json.loads(line)[5:] for line in self.log.read_text().splitlines()]

    def test_first_deployment_resets_schema_then_starts_app(self):
        result = self.run_deploy()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(
            hashlib.sha256(self.schema.read_bytes()).hexdigest(), self.checksum.read_text().strip()
        )
        sql = self.sql.read_text()
        self.assertTrue(sql.startswith("DROP SCHEMA public CASCADE;\nCREATE SCHEMA public;\n"))
        self.assertIn(self.schema.read_text(), sql)
        commands = self.commands()
        self.assertLess(commands.index(["stop", "app"]), commands.index(["up", "-d", "--wait", "database"]))
        execution = next(command for command in commands if command[0] == "exec")
        self.assertIn("--single-transaction", execution[-1])
        self.assertIn("ON_ERROR_STOP=on", execution[-1])
        self.assertEqual(["up", "-d", "--wait"], commands[-1])

    def test_changed_schema_replaces_previous_checksum(self):
        self.checksum.write_text("previous-checksum\n")
        result = self.run_deploy()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(
            hashlib.sha256(self.schema.read_bytes()).hexdigest(), self.checksum.read_text().strip()
        )
        self.assertIn(["stop", "app"], self.commands())
        self.assertTrue(self.sql.exists())

    def test_unchanged_schema_preserves_data(self):
        self.checksum.write_text(hashlib.sha256(self.schema.read_bytes()).hexdigest() + "\n")
        result = self.run_deploy()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(
            [["config", "--quiet"], ["pull"], ["up", "-d", "--wait"]], self.commands()
        )
        self.assertFalse(self.sql.exists())

    def test_sql_failure_keeps_previous_checksum_and_does_not_restart_app(self):
        self.checksum.write_text("previous-checksum\n")
        result = self.run_deploy(FAIL_SQL="1")
        self.assertNotEqual(0, result.returncode)
        self.assertEqual("previous-checksum\n", self.checksum.read_text())
        self.assertNotIn(["up", "-d", "--wait"], self.commands())
        self.assertEqual("exec", self.commands()[-1][0])

    def test_pull_failure_does_not_stop_app_or_record_checksum(self):
        result = self.run_deploy(FAIL_PULL="1")
        self.assertNotEqual(0, result.returncode)
        self.assertFalse(self.checksum.exists())
        self.assertEqual([["config", "--quiet"], ["pull"]], self.commands())


if __name__ == "__main__":
    unittest.main()

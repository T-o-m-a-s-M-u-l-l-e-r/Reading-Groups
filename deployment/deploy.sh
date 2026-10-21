#!/usr/bin/env bash
set -euo pipefail

# Run from the deployment directory after CI copies the release files here.
compose=(docker compose -f compose.yaml -f compose.prod.yaml)
schema_file=deployment/schema.sql
checksum_file=deployment/.schema.sha256

"${compose[@]}" config --quiet
schema_checksum=$(sha256sum "$schema_file" | cut -d ' ' -f 1)
applied_checksum=""
if [[ -f "$checksum_file" ]]; then
    applied_checksum=$(cat "$checksum_file")
fi

# Download images before interrupting the running app.
"${compose[@]}" pull

if [[ "$schema_checksum" != "$applied_checksum" ]]; then
    echo "Schema changed or no previous checksum exists; resetting application data."
    "${compose[@]}" stop app
    "${compose[@]}" up -d --wait database

    # Read the uploaded SQL from the host, rather than a potentially older bind-mounted inode.
    # ON_ERROR_STOP and a single transaction preserve the old schema if any statement fails.
    {
        printf 'DROP SCHEMA public CASCADE;\nCREATE SCHEMA public;\n'
        cat "$schema_file"
    } | "${compose[@]}" exec -T database sh -c \
        'exec psql -X --set ON_ERROR_STOP=on --single-transaction -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f -'

    printf '%s\n' "$schema_checksum" > "$checksum_file"
else
    echo "Schema unchanged; preserving application data."
fi

"${compose[@]}" up -d --wait

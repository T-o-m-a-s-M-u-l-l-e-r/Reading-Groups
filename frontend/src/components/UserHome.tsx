import { useState } from "react"
import GroupSelector, { type Group } from "./GroupSelector"
import PdfViewer from "./PdfViewer"
import CreateGroupButton from "./CreateGroupButton"
import CommentViewer from "./CommentViewer"
import CreateCommentButton from "./CreateCommentButton"
import GroupJoinLinkPanel from "./GroupJoinLinkPanel"
import EmailVerificationBanner from "./EmailVerificationBanner"

function UserHome() {
  const [selectedGroup, setSelectedGroup] = useState<Group | null>(null)
  const [groupsRefresh, setGroupsRefresh] = useState(false)
  const [commentsRefresh, setCommentsRefresh] = useState(false)

  return (
    <div className="user-dashboard">
      <EmailVerificationBanner />
      <main className="user-home">
        <div className="main-column">
          <section className="pdf-area">
            <PdfViewer selectedGroup={selectedGroup} />
          </section>

          <section className="comments-area">
            <CommentViewer
              selectedGroup={selectedGroup}
              commentsRefresh={commentsRefresh}
            />

            <div className="comment-actions">
              <CreateCommentButton
                selectedGroup={selectedGroup}
                onCommentCreated={() =>
                  setCommentsRefresh(value => !value)
                }
              />
            </div>
          </section>
        </div>

        <aside className="sidebar">
          <h3 className="groups-heading">My Groups</h3>

          <div className="group-area">
            <GroupSelector
              selectedGroup={selectedGroup}
              setSelectedGroup={setSelectedGroup}
              groupsRefresh={groupsRefresh}
            />
          </div>

          {selectedGroup && <GroupJoinLinkPanel key={selectedGroup.id} group={selectedGroup} />}

          <div className="create-area">
            <CreateGroupButton onGroupCreated={() => setGroupsRefresh(value => !value)} />
          </div>
        </aside>
      </main>
    </div>
  )
}

export default UserHome

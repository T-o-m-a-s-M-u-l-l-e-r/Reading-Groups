import GroupIcon from "./GroupIcon"
import "./GroupIcon.css"
import type { Group } from "./GroupSelector"

type GroupListItemProps = {
  group: Group,
  setSelectedGroup: (group: Group) => void,
  isSelected: boolean
}

function getGroupAge(createdAt: string) {
  const created = new Date(createdAt)
  const now = new Date()

  const differenceMs = now.getTime() - created.getTime()
  const days = Math.floor(differenceMs / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return "Created today"
  }

  if (days === 1) {
    return "Created yesterday"
  }

  return `Created ${days} days ago`
}

function GroupListItem({ group, setSelectedGroup, isSelected }: GroupListItemProps) {
  return (
    <li className="group-list-item">
      <button
        className={`group-list-item-button ${isSelected ? "selected" : ""}`}
        onClick={() => setSelectedGroup(group)}
      >
        <div className="group-list-heading">
          <GroupIcon key={group.id} groupId={group.id} hasIcon={group.hasIcon} />
          <h3>{group.groupName}</h3>
        </div>

        <p className="group-creator">
          Created by {group.administratorName}
        </p>

        <div className="group-meta">
          <span>{group.memberCount} members</span>
          <span>{getGroupAge(group.createdAt)}</span>
        </div>
      </button>
    </li>
  )
}

export default GroupListItem
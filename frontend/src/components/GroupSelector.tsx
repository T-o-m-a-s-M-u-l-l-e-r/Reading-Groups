import { useEffect, useState } from "react"
import { useApi } from "../api/apiFetch"
import GroupListItem from "./GroupListItem"

export type Group = {
  id: number,
  hasIcon: boolean,
  administratorUserId: number,
  createdAt: string,
  groupName: string,
  memberCount: number,
  administratorName: string
}

type GroupSelectorProps = {
  setSelectedGroup: (group: Group) => void,
  selectedGroup: Group | null,
  groupsRefresh: boolean
}

function GroupSelector({ setSelectedGroup, selectedGroup, groupsRefresh }: GroupSelectorProps) {
  const [groups, setGroups] = useState<Group[]>([])
  const apiFetch = useApi()

  useEffect(() => {
    async function loadGroups() {
      const response = await apiFetch("/api/groups", {
        method: "GET"
      })

      if (response) {
        const data = await response.json()
        setGroups(data)
      }
    }

    loadGroups()
  }, [groupsRefresh])

  return (
    <div className="group-selector">
      <ul className="group-list">
        {groups.map(group => (
          <GroupListItem
            key={group.id}
            group={group}
            setSelectedGroup={setSelectedGroup}
            isSelected={selectedGroup?.id === group.id}
          />
        ))}
      </ul>
    </div>
  )
}

export default GroupSelector
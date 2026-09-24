import { Button, Input, Select } from "../../../components/ui";
import { roleLabels, userStatusLabels } from "../../../utils/enumLabels";
import { userRoles, userStatuses } from "../model/userModel";
import { FacultyCombobox } from "./FacultyCombobox";

export function UserFilters({
  filters,
  onChange,
  onSubmit,
  showStatus = true,
}) {
  return (
    <form className="filter-bar" onSubmit={onSubmit}>
      <Input
        label="Tìm kiếm"
        name="keyword"
        placeholder="Mã giảng viên, họ tên, email"
        value={filters.keyword}
        onChange={(event) =>
          onChange({ ...filters, keyword: event.target.value })
        }
      />
      <Select
        label="Vai trò"
        name="role"
        value={filters.role}
        onChange={(event) =>
          onChange({ ...filters, role: event.target.value })
        }
        options={[
          { value: "", label: "Tất cả vai trò" },
          ...userRoles.map((value) => ({
            value,
            label: roleLabels[value] || value,
          })),
        ]}
      />
      {showStatus && (
        <Select
          label="Trạng thái"
          name="status"
          value={filters.status}
          onChange={(event) =>
            onChange({ ...filters, status: event.target.value })
          }
          options={[
            { value: "", label: "Tất cả trạng thái" },
            ...userStatuses.map((value) => ({
              value,
              label: userStatusLabels[value] || value,
            })),
          ]}
        />
      )}
      <FacultyCombobox
        label="Khoa"
        allowClear
        value={filters.facultyId}
        onChange={(facultyId) => onChange({ ...filters, facultyId })}
      />
      <Button type="submit">Áp dụng</Button>
    </form>
  );
}

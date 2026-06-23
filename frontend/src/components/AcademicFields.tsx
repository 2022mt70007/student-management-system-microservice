import { useEffect, useState } from 'react';
import { listClasses, listDepartments, listSubjects } from '../api/academic';
import type { AcademicClassResponse, DepartmentResponse, SubjectResponse } from '../types';

interface AcademicFieldsProps {
  departmentId: number | '';
  classId: number | '';
  subjectIds: number[];
  onDepartmentChange: (departmentId: number | '') => void;
  onClassChange: (classId: number | '') => void;
  onSubjectIdsChange: (subjectIds: number[]) => void;
}

export function AcademicFields({
  departmentId,
  classId,
  subjectIds,
  onDepartmentChange,
  onClassChange,
  onSubjectIdsChange,
}: AcademicFieldsProps) {
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [classes, setClasses] = useState<AcademicClassResponse[]>([]);
  const [subjects, setSubjects] = useState<SubjectResponse[]>([]);
  const [loadingDepartments, setLoadingDepartments] = useState(true);
  const [loadingClasses, setLoadingClasses] = useState(false);
  const [loadingSubjects, setLoadingSubjects] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    listDepartments(true)
      .then(setDepartments)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load departments'))
      .finally(() => setLoadingDepartments(false));
  }, []);

  useEffect(() => {
    if (!departmentId) {
      setClasses([]);
      return;
    }
    setLoadingClasses(true);
    listClasses(Number(departmentId))
      .then(setClasses)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load classes'))
      .finally(() => setLoadingClasses(false));
  }, [departmentId]);

  useEffect(() => {
    if (!classId) {
      setSubjects([]);
      return;
    }
    setLoadingSubjects(true);
    listSubjects(Number(classId))
      .then(setSubjects)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load subjects'))
      .finally(() => setLoadingSubjects(false));
  }, [classId]);

  function handleDepartmentChange(value: string) {
    const next = value ? Number(value) : '';
    onDepartmentChange(next);
    onClassChange('');
    onSubjectIdsChange([]);
  }

  function handleClassChange(value: string) {
    const next = value ? Number(value) : '';
    onClassChange(next);
    onSubjectIdsChange([]);
  }

  function toggleSubject(id: number) {
    if (subjectIds.includes(id)) {
      onSubjectIdsChange(subjectIds.filter((s) => s !== id));
    } else {
      onSubjectIdsChange([...subjectIds, id]);
    }
  }

  return (
    <div className="academic-fields">
      {error && <p className="alert alert-error">{error}</p>}
      <label>
        Department *
        <select
          value={departmentId}
          onChange={(e) => handleDepartmentChange(e.target.value)}
          required
          disabled={loadingDepartments}
        >
          <option value="">Select department</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>{d.departmentName}</option>
          ))}
        </select>
      </label>
      <label>
        Class *
        <select
          value={classId}
          onChange={(e) => handleClassChange(e.target.value)}
          required
          disabled={!departmentId || loadingClasses}
        >
          <option value="">Select class</option>
          {classes.map((c) => (
            <option key={c.id} value={c.id}>{c.className}</option>
          ))}
        </select>
      </label>
      <div className="full-width">
        <span className="field-label">Subjects *</span>
        {!classId ? (
          <p className="empty-state">Select a class to load subjects.</p>
        ) : loadingSubjects ? (
          <p className="empty-state">Loading subjects...</p>
        ) : subjects.length === 0 ? (
          <p className="empty-state">No subjects defined for this class.</p>
        ) : (
          <div className="checkbox-list">
            {subjects.map((s) => (
              <label key={s.id} className="checkbox-item">
                <input
                  type="checkbox"
                  checked={subjectIds.includes(s.id)}
                  onChange={() => toggleSubject(s.id)}
                />
                <span>{s.subjectName}{s.credits != null ? ` (${s.credits} cr)` : ''}</span>
              </label>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

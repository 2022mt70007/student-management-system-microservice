interface AlertProps {
  type?: 'error' | 'success' | 'info';
  message: string;
}

export function Alert({ type = 'info', message }: AlertProps) {
  if (!message) return null;
  return <div className={`alert alert-${type}`}>{message}</div>;
}

export function LoadingSpinner({ label = 'Loading...' }: { label?: string }) {
  return (
    <div className="loading">
      <div className="spinner" />
      <span>{label}</span>
    </div>
  );
}

export function PageHeader({
  title,
  subtitle,
  action,
}: {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="page-header">
      <div>
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}

export function StatusBadge({ status }: { status: string }) {
  const normalized = status?.replace(/_/g, ' ') ?? '';
  const className =
    status === 'ACTIVE'
      ? 'badge badge-success'
      : status === 'PENDING_REGISTRATION'
        ? 'badge badge-warning'
        : 'badge badge-muted';
  return <span className={className}>{normalized}</span>;
}

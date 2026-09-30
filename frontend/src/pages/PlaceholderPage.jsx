import React from 'react';
import { FolderOpen } from 'lucide-react';

const PlaceholderPage = ({ title, description, icon: Icon = FolderOpen }) => {
  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.6rem', fontWeight: 800 }}>{title}</h1>
        {description && <p style={{ color: 'var(--text-muted)' }}>{description}</p>}
      </div>

      <div className="card">
        <div className="empty-state">
          <Icon size={40} />
          <p>Nothing to show here yet.</p>
        </div>
      </div>
    </div>
  );
};

export default PlaceholderPage;

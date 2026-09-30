import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import assessmentHostService from '../../services/assessmentHostService';
import {
  ShieldCheck,
  UploadCloud,
  AlertCircle,
  CheckCircle,
  Clock,
  XCircle,
  KeyRound,
  PlusCircle,
  Edit,
  Trash2,
  ClipboardList,
  ListChecks,
  ArrowLeft,
  Send,
  Save,
  X,
  Building2,
  Users,
  Download,
  FileText,
  Info,
  ExternalLink,
  MessageSquare,
  Star,
} from 'lucide-react';

const STATUS_STYLES = {
  DRAFT: { color: 'var(--text-muted)', bg: 'var(--bg-input)', label: 'Draft' },
  PUBLISHED: { color: 'var(--primary)', bg: 'var(--primary-soft)', label: 'Published' },
  ARCHIVED: { color: 'var(--text-subtle)', bg: 'var(--bg-input)', label: 'Archived' },
};

const ORG_TYPES = [
  'Private Company',
  'Public Company',
  'Startup',
  'Educational Institution',
  'EdTech Company',
  'Government Organization',
  'Non-Profit Organization',
  'Other',
];

const ASSESSMENT_PURPOSES = [
  'Hiring / Recruitment',
  'Campus Placement',
  'Internal Employee Training / Evaluation',
  'Student Contest / Hackathon',
  'Skill Certification',
  'Other',
];

const emptyAssessmentForm = { title: '', description: '', instructions: '', durationMinutes: 30, passingMarks: 0 };
const makeEmptyOption = () => ({ id: null, optionText: '', isCorrect: false });
const emptyQuestionForm = () => ({
  id: null,
  questionText: '',
  questionType: 'MCQ',
  marks: 1,
  orderIndex: 1,
  explanation: '',
  options: [makeEmptyOption(), makeEmptyOption()],
  // PROGRAMMING-specific fields
  programmingQuestion: {
    problemStatement: '',
    inputFormat: '',
    outputFormat: '',
    constraints: '',
    sampleInput: '',
    sampleOutput: '',
    timeLimitMs: 2000,
    memoryLimitMb: 256,
    supportedLanguages: ['java', 'python', 'cpp'],
  },
});

import { useAuth } from '../../context/AuthContext';

/**
 * "Host Assessment" entry point for users and admins. Gated by the assessment-host
 * business verification workflow (see AssessmentHostVerificationController /
 * AssessmentHostVerificationService on the backend).
 *
 * Lifecycle:
 *  - Unverified users see VerificationGate (submit organization details + docs / under review / code entry).
 *  - Once verified (status === 'VERIFIED') or if admin, the user goes DIRECTLY to
 *    HostAssessmentBuilder. They are never asked for the verification code again,
 *    and can create multiple assessments without re-verification.
 */
const HostAssessment = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ROLE_ADMIN' || user?.role === 'ADMIN';
  const [verification, setVerification] = useState(null);
  const [loadingStatus, setLoadingStatus] = useState(!isAdmin);
  const [statusError, setStatusError] = useState('');

  const loadStatus = async () => {
    try {
      setLoadingStatus(true);
      setStatusError('');
      const data = await assessmentHostService.getVerificationStatus();
      setVerification(data);
    } catch (err) {
      setStatusError(err.response?.data?.message || 'Unable to load your verification status.');
    } finally {
      setLoadingStatus(false);
    }
  };

  useEffect(() => {
    if (!isAdmin) {
      loadStatus();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAdmin]);

  const isVerified = isAdmin || verification?.status === 'VERIFIED';

  if (loadingStatus && !isAdmin) {
    return (
      <div>
        <div style={{ marginBottom: '1.5rem' }}>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ShieldCheck size={26} color="#8b5cf6" /> Host Assessment
          </h1>
        </div>
        <div className="card">
          <div style={{ textAlign: 'center', padding: '2.5rem 0', color: 'var(--text-muted)' }}>
            Loading verification status...
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Building2 size={26} color="#8b5cf6" /> Host Assessment
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>
            {isVerified
              ? 'Create, manage, and publish skill assessments you host for your organization.'
              : 'Complete Business & Organization Verification to host assessments on CodeNova.'}
          </p>
        </div>
        {isVerified && (
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            padding: '0.35rem 0.8rem',
            borderRadius: '9999px',
            backgroundColor: 'rgba(34, 197, 94, 0.15)',
            color: '#15803d',
            fontWeight: 600,
            fontSize: '0.85rem',
          }}>
            <CheckCircle size={16} /> Verified Host
          </span>
        )}
      </div>

      {!isVerified ? (
        <VerificationGate
          verification={verification}
          setVerification={setVerification}
          error={statusError}
          setError={setStatusError}
          onVerified={(data) => setVerification(data)}
        />
      ) : (
        <HostAssessmentBuilder />
      )}
    </div>
  );
};

// =====================================================================
// Verification gate: Business / Organization Verification Form
// =====================================================================

const VerificationGate = ({ verification, setVerification, error, setError, onVerified }) => {
  const [formData, setFormData] = useState({
    organizationName: '',
    organizationType: 'Private Company',
    registrationNumber: '',
    officialEmail: '',
    country: '',
    website: '',
    representativeName: '',
    contactNumber: '',
    purpose: 'Hiring / Recruitment',
  });

  const [primaryFile, setPrimaryFile] = useState(null);
  const [gstFile, setGstFile] = useState(null);
  const [authLetterFile, setAuthLetterFile] = useState(null);
  const [supportingFile, setSupportingFile] = useState(null);

  const [searchParams] = useSearchParams();
  const urlCode = searchParams.get('code') || '';

  const [submitting, setSubmitting] = useState(false);
  const [code, setCode] = useState(urlCode);
  const [codeError, setCodeError] = useState('');
  const [verifyingCode, setVerifyingCode] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [autoVerifyTriggered, setAutoVerifyTriggered] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.organizationName.trim()) {
      setError('Legal Organization / Company Name is required.');
      return;
    }
    if (!formData.registrationNumber.trim()) {
      setError('Official Registration Number / Incorporation ID is required.');
      return;
    }
    if (!formData.officialEmail.trim() || !formData.officialEmail.includes('@')) {
      setError('A valid Official Organization Email is required.');
      return;
    }
    if (!formData.country.trim()) {
      setError('Country of Operation / Registration is required.');
      return;
    }
    if (!formData.website.trim()) {
      setError('Official Website / Portfolio / LinkedIn URL is required.');
      return;
    }
    if (!formData.representativeName.trim()) {
      setError('Authorized Representative Full Name is required.');
      return;
    }
    if (!formData.contactNumber.trim()) {
      setError('Official Representative Contact Number is required.');
      return;
    }
    if (!primaryFile) {
      setError('Primary Business Verification Document is required (Certificate of Incorporation, Registration, or Affiliation Letter).');
      return;
    }
    if (primaryFile.size > 5 * 1024 * 1024) {
      setError('Primary verification document exceeds the 5MB size limit.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const payload = new FormData();
      Object.entries(formData).forEach(([k, v]) => payload.append(k, v));
      payload.append('primaryDocument', primaryFile);
      if (gstFile) payload.append('gstCertificate', gstFile);
      if (authLetterFile) payload.append('authorizationLetter', authLetterFile);
      if (supportingFile) payload.append('supportingDocument', supportingFile);

      const res = await assessmentHostService.submitVerification(payload);
      setVerification(res);
      const emailNote = res.emailDeliveryStatus === 'SENT'
        ? ' A confirmation email has been sent to your registered email address.'
        : '';
      setSuccessMessage(`Your organization verification request has been submitted for review.${emailNote}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to submit verification request. Please check all fields.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleVerifyCode = async (e) => {
    e.preventDefault();
    if (!code.trim()) {
      setCodeError('Please enter the verification code from your email.');
      return;
    }
    setVerifyingCode(true);
    setCodeError('');
    try {
      const data = await assessmentHostService.verifyCode(code.trim());
      setCode('');
      if (onVerified) {
        onVerified(data);
      } else {
        setVerification(data);
      }
    } catch (err) {
      setCodeError(err.response?.data?.message || 'Invalid or expired verification code.');
    } finally {
      setVerifyingCode(false);
    }
  };

  const status = verification?.status || 'NOT_SUBMITTED';

  useEffect(() => {
    if (urlCode && !code) {
      setCode(urlCode);
    }
  }, [urlCode]);

  useEffect(() => {
    // If status is APPROVED and code is present in URL, auto-verify immediately
    if (status === 'APPROVED' && urlCode && !autoVerifyTriggered && !verifyingCode) {
      setAutoVerifyTriggered(true);
      (async () => {
        setVerifyingCode(true);
        setCodeError('');
        try {
          const data = await assessmentHostService.verifyCode(urlCode.trim());
          if (onVerified) {
            onVerified(data);
          } else {
            setVerification(data);
          }
        } catch (err) {
          setCodeError(err.response?.data?.message || 'Invalid or expired verification code.');
        } finally {
          setVerifyingCode(false);
        }
      })();
    }
  }, [status, urlCode]);

  return (
    <div className="card" style={{ marginBottom: '1.5rem', maxWidth: '900px', margin: '0 auto 1.5rem' }}>
      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}
      {successMessage && (
        <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
          <CheckCircle size={18} />
          <span>{successMessage}</span>
        </div>
      )}

      {status === 'VERIFIED' && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <CheckCircle size={20} color="#15803d" />
          <div>
            <strong style={{ color: '#15803d' }}>Organization Verified</strong>
            <p style={{ color: 'var(--text-muted)', margin: 0 }}>
              Your account is permanently verified as an assessment host representing {verification?.organizationName || 'your organization'}.
            </p>
          </div>
        </div>
      )}

      {status === 'PENDING' && (
        <div style={{ padding: '1rem', border: '1px solid rgba(245, 158, 11, 0.3)', borderRadius: '8px', backgroundColor: 'rgba(245, 158, 11, 0.05)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1rem' }}>
            <Clock size={22} color="#b45309" />
            <div>
              <strong style={{ color: '#b45309', fontSize: '1.1rem' }}>Business Verification Request Under Review</strong>
              <p style={{ color: 'var(--text-muted)', margin: 0 }}>
                An administrator is reviewing your organization credentials and primary document.
              </p>
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '0.75rem', fontSize: '0.9rem', marginTop: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
            <div><strong>Organization:</strong> {verification?.organizationName || '-'}</div>
            <div><strong>Type:</strong> {verification?.organizationType || '-'}</div>
            <div><strong>Registration No:</strong> {verification?.registrationNumber || '-'}</div>
            <div><strong>Official Email:</strong> {verification?.officialEmail || '-'}</div>
            <div><strong>Representative:</strong> {verification?.representativeName || '-'}</div>
            <div><strong>Contact:</strong> {verification?.contactNumber || '-'}</div>
            <div><strong>Country:</strong> {verification?.country || '-'}</div>
            <div><strong>Purpose:</strong> {verification?.purpose || '-'}</div>
            <div><strong>Primary Document:</strong> {verification?.documentOriginalName || 'Uploaded'}</div>
          </div>
        </div>
      )}

      {status === 'APPROVED' && (
        <div style={{ padding: '1.25rem', border: '1px solid rgba(139, 92, 246, 0.3)', borderRadius: '8px', backgroundColor: 'rgba(139, 92, 246, 0.05)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1rem' }}>
            <KeyRound size={24} color="var(--primary)" />
            <div>
              <strong style={{ fontSize: '1.1rem' }}>Organization Verification Approved!</strong>
              <p style={{ color: 'var(--text-muted)', margin: 0 }}>
                Your business credentials for <strong>{verification?.organizationName}</strong> have been verified by the administrator.
                A one-time verification code has been sent to your registered email. Enter it below to complete host activation permanently.
              </p>
            </div>
          </div>
          <form onSubmit={handleVerifyCode} style={{ display: 'flex', gap: '0.6rem', flexWrap: 'wrap', alignItems: 'flex-start' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label style={{ fontWeight: 600 }}>6-Digit Verification Code</label>
              <input
                type="text"
                className="form-control"
                placeholder="e.g. 414776 or CN-XXXXXX"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                style={{ maxWidth: '280px', letterSpacing: '0.1em', fontWeight: 600 }}
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={verifyingCode} style={{ marginTop: '1.6rem' }}>
              {verifyingCode ? 'Activating...' : 'Activate Host Status'}
            </button>
          </form>
          {codeError && <p style={{ color: 'var(--danger)', fontSize: '0.85rem', marginTop: '0.5rem' }}>{codeError}</p>}
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.6rem', marginBottom: 0 }}>
            💡 <em>Didn't see the email? Please check your Spam, Junk, or Promotions folder, or search for "CodeNova".</em>
          </p>
        </div>
      )}

      {(status === 'NOT_SUBMITTED' || status === 'REJECTED') && (
        <div>
          {status === 'REJECTED' && (
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.6rem', marginBottom: '1.25rem', padding: '1rem', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: '8px', backgroundColor: 'rgba(239, 68, 68, 0.05)' }}>
              <XCircle size={22} color="var(--danger)" />
              <div>
                <strong style={{ color: 'var(--danger)' }}>Previous Verification Request Rejected</strong>
                <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0' }}>
                  <strong>Reason:</strong> {verification?.rejectionReason || 'Details could not be authenticated.'}
                </p>
                <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0', fontSize: '0.85rem' }}>
                  Please review the feedback, ensure your registration certificate is clear, and resubmit below.
                </p>
              </div>
            </div>
          )}

          <div style={{ padding: '1rem', borderRadius: '8px', backgroundColor: 'var(--primary-soft)', marginBottom: '1.5rem', display: 'flex', gap: '0.75rem', alignItems: 'flex-start' }}>
            <Info size={22} color="var(--primary)" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div style={{ fontSize: '0.875rem' }}>
              <strong style={{ display: 'block', marginBottom: '0.25rem' }}>Why is Organization Verification Required?</strong>
              <p style={{ margin: 0, color: 'var(--text-muted)' }}>
                CodeNova maintains strict assessment integrity. To ensure students and candidates are tested with legitimate evaluations, all hosts must prove they represent a registered enterprise, academic institution, startup, or educational organization.
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem', marginBottom: '1.25rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '1rem' }}>
                <Building2 size={18} color="var(--primary)" /> 1. Organization Information
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
                <div className="form-group">
                  <label>Legal Organization / Company Name <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <input
                    type="text"
                    name="organizationName"
                    className="form-control"
                    placeholder="e.g. Acme Tech Solutions Pvt Ltd"
                    value={formData.organizationName}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Organization Type <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <select
                    name="organizationType"
                    className="form-control"
                    value={formData.organizationType}
                    onChange={handleChange}
                    required
                  >
                    {ORG_TYPES.map((type) => (
                      <option key={type} value={type}>{type}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Registration Number / Incorporation ID <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <input
                    type="text"
                    name="registrationNumber"
                    className="form-control"
                    placeholder="e.g. CIN / UEN / Reg # / AISHE code"
                    value={formData.registrationNumber}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Official Organization Email <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <input
                    type="email"
                    name="officialEmail"
                    className="form-control"
                    placeholder="e.g. hr@acmetech.com or placement@college.edu"
                    value={formData.officialEmail}
                    onChange={handleChange}
                    required
                  />
                  <small style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>Use company or institutional domain email</small>
                </div>

                <div className="form-group">
                  <label>Country of Operation / Registration <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <input
                    type="text"
                    name="country"
                    className="form-control"
                    placeholder="e.g. India, United States, United Kingdom"
                    value={formData.country}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Official Website / Profile URL <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <input
                    type="url"
                    name="website"
                    className="form-control"
                    placeholder="https://www.company.com or LinkedIn page"
                    value={formData.website}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
            </div>

            <div style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem', marginBottom: '1.25rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '1rem' }}>
                <Users size={18} color="var(--primary)" /> 2. Representative & Assessment Purpose
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
                <div className="form-group">
                  <label>Authorized Representative Full Name <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <input
                    type="text"
                    name="representativeName"
                    className="form-control"
                    placeholder="Full name of host"
                    value={formData.representativeName}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Official Contact Number <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <input
                    type="tel"
                    name="contactNumber"
                    className="form-control"
                    placeholder="+91 9876543210"
                    value={formData.contactNumber}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                  <label>Primary Assessment Purpose <span style={{ color: 'var(--danger)' }}>*</span></label>
                  <select
                    name="purpose"
                    className="form-control"
                    value={formData.purpose}
                    onChange={handleChange}
                    required
                  >
                    {ASSESSMENT_PURPOSES.map((purpose) => (
                      <option key={purpose} value={purpose}>{purpose}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.5rem' }}>
                <UploadCloud size={18} color="var(--primary)" /> 3. Verification Documents
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Accepted formats: PDF, JPG, JPEG, PNG (Max 5MB per document). Documents are kept confidential and accessed only by platform administrators.
              </p>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem' }}>
                <div className="form-group" style={{ border: '1px dashed var(--border-color)', padding: '1rem', borderRadius: '8px' }}>
                  <label style={{ fontWeight: 600 }}>
                    Primary Business Registration Certificate <span style={{ color: 'var(--danger)' }}>*</span>
                  </label>
                  <p style={{ fontSize: '0.78rem', color: 'var(--text-subtle)', margin: '0.2rem 0 0.6rem' }}>
                    Certificate of Incorporation, Business License, Society Registration, or College Affiliation Letter
                  </p>
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png"
                    onChange={(e) => setPrimaryFile(e.target.files?.[0] || null)}
                    required
                  />
                </div>

                <div className="form-group" style={{ border: '1px dashed var(--border-color)', padding: '1rem', borderRadius: '8px' }}>
                  <label style={{ fontWeight: 600 }}>GST / Tax Certificate <span style={{ color: 'var(--text-subtle)', fontWeight: 400 }}>(Optional)</span></label>
                  <p style={{ fontSize: '0.78rem', color: 'var(--text-subtle)', margin: '0.2rem 0 0.6rem' }}>
                    Official GSTIN certificate or country tax identification document
                  </p>
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png"
                    onChange={(e) => setGstFile(e.target.files?.[0] || null)}
                  />
                </div>

                <div className="form-group" style={{ border: '1px dashed var(--border-color)', padding: '1rem', borderRadius: '8px' }}>
                  <label style={{ fontWeight: 600 }}>Official Authorization Letter <span style={{ color: 'var(--text-subtle)', fontWeight: 400 }}>(Optional)</span></label>
                  <p style={{ fontSize: '0.78rem', color: 'var(--text-subtle)', margin: '0.2rem 0 0.6rem' }}>
                    Letter on organization letterhead delegating assessment hosting authority
                  </p>
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png"
                    onChange={(e) => setAuthLetterFile(e.target.files?.[0] || null)}
                  />
                </div>

                <div className="form-group" style={{ border: '1px dashed var(--border-color)', padding: '1rem', borderRadius: '8px' }}>
                  <label style={{ fontWeight: 600 }}>Other Supporting Document <span style={{ color: 'var(--text-subtle)', fontWeight: 400 }}>(Optional)</span></label>
                  <p style={{ fontSize: '0.78rem', color: 'var(--text-subtle)', margin: '0.2rem 0 0.6rem' }}>
                    Company brochure, accreditation certificate, or partnership agreement
                  </p>
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png"
                    onChange={(e) => setSupportingFile(e.target.files?.[0] || null)}
                  />
                </div>
              </div>
            </div>

            <button type="submit" className="btn btn-primary" disabled={submitting} style={{ width: '100%', padding: '0.75rem', fontSize: '1rem' }}>
              <UploadCloud size={18} />
              <span>{submitting ? 'Submitting Organization Details...' : 'Submit Organization for Verification'}</span>
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

// =====================================================================
// Assessment builder for verified hosts - mirrors the admin authoring UI
// (AssessmentManagement.jsx) but scoped to the current host's own
// assessments via assessmentHostService's /assessments/host endpoints.
// =====================================================================

const HostAssessmentBuilder = () => {
  const [assessments, setAssessments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busyAssessmentId, setBusyAssessmentId] = useState(null);

  const [isAssessmentModalOpen, setIsAssessmentModalOpen] = useState(false);
  const [editingAssessmentId, setEditingAssessmentId] = useState(null);
  const [editingAssessmentTotalMarks, setEditingAssessmentTotalMarks] = useState(null);
  const [assessmentForm, setAssessmentForm] = useState(emptyAssessmentForm);
  const [assessmentFormErrors, setAssessmentFormErrors] = useState([]);
  const [savingAssessment, setSavingAssessment] = useState(false);

  const [manageAssessment, setManageAssessment] = useState(null);
  const [questionsLoading, setQuestionsLoading] = useState(false);
  const [questionsError, setQuestionsError] = useState('');
  const [busyQuestionId, setBusyQuestionId] = useState(null);

  const [questionFormOpen, setQuestionFormOpen] = useState(false);
  const [questionForm, setQuestionForm] = useState(emptyQuestionForm());
  const [originalOptionIds, setOriginalOptionIds] = useState([]);
  const [questionFormErrors, setQuestionFormErrors] = useState([]);
  const [savingQuestion, setSavingQuestion] = useState(false);

  // Candidate attempts modal
  const [candidateAttemptsModalOpen, setCandidateAttemptsModalOpen] = useState(false);
  const [selectedAssessmentForAttempts, setSelectedAssessmentForAttempts] = useState(null);
  const [attemptsList, setAttemptsList] = useState([]);
  const [loadingAttempts, setLoadingAttempts] = useState(false);
  const [attemptsError, setAttemptsError] = useState('');

  const openAttemptsModal = async (assessment) => {
    setSelectedAssessmentForAttempts(assessment);
    setCandidateAttemptsModalOpen(true);
    setLoadingAttempts(true);
    setAttemptsError('');
    try {
      const data = await assessmentHostService.getHostAssessmentAttempts(assessment.id);
      setAttemptsList(data || []);
    } catch (err) {
      setAttemptsError(err.response?.data?.message || 'Failed to load candidate attempts.');
    } finally {
      setLoadingAttempts(false);
    }
  };

  const closeAttemptsModal = () => {
    setCandidateAttemptsModalOpen(false);
    setSelectedAssessmentForAttempts(null);
    setAttemptsList([]);
  };

  const exportAttemptsCSV = () => {
    if (!attemptsList.length) return;
    const headers = ['Attempt ID', 'Candidate Name', 'Username', 'Email', 'Score', 'Total Marks', 'Passing Marks', 'Status', 'Result', 'Started At', 'Completed At'];
    const rows = attemptsList.map((a) => [
      a.id,
      `"${(a.candidateFullName || '').replace(/"/g, '""')}"`,
      `"${(a.candidateUsername || '').replace(/"/g, '""')}"`,
      `"${(a.candidateEmail || '').replace(/"/g, '""')}"`,
      a.score ?? '',
      a.totalMarks ?? '',
      a.passingMarks ?? '',
      a.status ?? '',
      a.passed ? 'PASSED' : (a.passed === false ? 'FAILED' : 'PENDING'),
      a.startedAt ? new Date(a.startedAt).toLocaleString() : '',
      a.completedAt ? new Date(a.completedAt).toLocaleString() : '',
    ]);
    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `assessment_${selectedAssessmentForAttempts?.id}_candidates.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Host Assessment Feedback modal
  const [feedbackModalOpen, setFeedbackModalOpen] = useState(false);
  const [selectedAssessmentForFeedback, setSelectedAssessmentForFeedback] = useState(null);
  const [feedbackSummary, setFeedbackSummary] = useState({ totalFeedback: 0, averageRating: 0.0, feedbacks: [] });
  const [loadingFeedback, setLoadingFeedback] = useState(false);
  const [feedbackError, setFeedbackError] = useState('');

  const openFeedbackModal = async (assessment) => {
    setSelectedAssessmentForFeedback(assessment);
    setFeedbackModalOpen(true);
    setLoadingFeedback(true);
    setFeedbackError('');
    try {
      const data = await assessmentHostService.getHostAssessmentFeedback(assessment.id);
      setFeedbackSummary(data || { totalFeedback: 0, averageRating: 0.0, feedbacks: [] });
    } catch (err) {
      setFeedbackError(err.response?.data?.message || 'Failed to load assessment feedback.');
    } finally {
      setLoadingFeedback(false);
    }
  };

  const closeFeedbackModal = () => {
    setFeedbackModalOpen(false);
    setSelectedAssessmentForFeedback(null);
    setFeedbackSummary({ totalFeedback: 0, averageRating: 0.0, feedbacks: [] });
  };

  useEffect(() => {
    fetchAssessments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchAssessments = async () => {
    try {
      setLoading(true);
      const data = await assessmentHostService.getMyHostedAssessments();
      setAssessments(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load your assessments.');
    } finally {
      setLoading(false);
    }
  };

  // ================= Create / Edit assessment =================

  const openCreateAssessmentModal = () => {
    setEditingAssessmentId(null);
    setEditingAssessmentTotalMarks(null);
    setAssessmentForm(emptyAssessmentForm);
    setAssessmentFormErrors([]);
    setError('');
    setIsAssessmentModalOpen(true);
  };

  const openEditAssessmentModal = (assessment) => {
    setEditingAssessmentId(assessment.id);
    setEditingAssessmentTotalMarks(assessment.totalMarks ?? 0);
    setAssessmentForm({
      title: assessment.title || '',
      description: assessment.description || '',
      instructions: assessment.instructions || '',
      durationMinutes: assessment.durationMinutes ?? 30,
      passingMarks: assessment.passingMarks ?? 0,
    });
    setAssessmentFormErrors([]);
    setError('');
    setIsAssessmentModalOpen(true);
  };

  const closeAssessmentModal = () => {
    if (savingAssessment) return;
    setIsAssessmentModalOpen(false);
  };

  const validateAssessmentForm = (form, totalMarksForEdit) => {
    const errors = [];
    if (!form.title.trim()) errors.push('Title is required.');
    if (!form.durationMinutes || Number(form.durationMinutes) <= 0) {
      errors.push('Duration must be greater than 0 minutes.');
    }
    if (form.passingMarks === '' || Number(form.passingMarks) < 0) {
      errors.push('Passing marks cannot be negative.');
    }
    if (totalMarksForEdit != null && totalMarksForEdit > 0 && Number(form.passingMarks) > totalMarksForEdit) {
      errors.push(`Passing marks cannot exceed the current total marks (${totalMarksForEdit}).`);
    }
    return errors;
  };

  const buildAssessmentPayload = (form) => ({
    title: form.title.trim(),
    description: form.description.trim(),
    instructions: form.instructions.trim(),
    durationMinutes: Number(form.durationMinutes),
    passingMarks: Number(form.passingMarks),
  });

  const handleAssessmentSubmit = async (e) => {
    e.preventDefault();
    const errors = validateAssessmentForm(assessmentForm, editingAssessmentTotalMarks);
    setAssessmentFormErrors(errors);
    if (errors.length > 0) return;

    setSavingAssessment(true);
    setError('');
    try {
      const payload = buildAssessmentPayload(assessmentForm);
      if (editingAssessmentId) {
        await assessmentHostService.updateHostedAssessment(editingAssessmentId, payload);
        setSuccess('Assessment updated successfully.');
      } else {
        await assessmentHostService.createHostedAssessment(payload);
        setSuccess('Assessment created successfully.');
      }
      setIsAssessmentModalOpen(false);
      fetchAssessments();
    } catch (err) {
      const backendErrors = err.response?.data?.errors;
      const message = backendErrors
        ? Object.values(backendErrors).join(' ')
        : err.response?.data?.message || 'Unable to save assessment.';
      setAssessmentFormErrors([message]);
    } finally {
      setSavingAssessment(false);
    }
  };

  // ================= Publish / Archive =================

  const handlePublish = async (assessment) => {
    if (!window.confirm(`Publish "${assessment.title}"? Students will be able to start it immediately.`)) return;
    setBusyAssessmentId(assessment.id);
    setError('');
    try {
      await assessmentHostService.publishHostedAssessment(assessment.id);
      setSuccess('Assessment published successfully.');
      fetchAssessments();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to publish assessment.');
    } finally {
      setBusyAssessmentId(null);
    }
  };

  const handleArchive = async (assessment) => {
    if (!window.confirm(`Archive "${assessment.title}"? Students will no longer be able to start new attempts.`)) return;
    setBusyAssessmentId(assessment.id);
    setError('');
    try {
      await assessmentHostService.archiveHostedAssessment(assessment.id);
      setSuccess('Assessment archived.');
      fetchAssessments();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to archive assessment.');
    } finally {
      setBusyAssessmentId(null);
    }
  };

  // ================= Manage Questions =================

  const openManageQuestions = async (assessment) => {
    setError('');
    setQuestionsError('');
    setQuestionFormOpen(false);
    setManageAssessment({ ...assessment, questions: assessment.questions || [] });
    setQuestionsLoading(true);
    try {
      const full = await assessmentHostService.getHostedAssessment(assessment.id);
      setManageAssessment(full);
    } catch (err) {
      setQuestionsError('Unable to load questions.');
    } finally {
      setQuestionsLoading(false);
    }
  };

  const closeManageQuestions = () => {
    if (savingQuestion) return;
    setManageAssessment(null);
    setQuestionFormOpen(false);
  };

  const refreshManageAssessment = async (assessmentId) => {
    try {
      const full = await assessmentHostService.getHostedAssessment(assessmentId);
      setManageAssessment(full);
    } catch (err) {
      setQuestionsError('Unable to refresh questions.');
    }
  };

  const handleDeleteQuestion = async (question) => {
    if (!window.confirm('Delete this question?')) return;
    setBusyQuestionId(question.id);
    setQuestionsError('');
    try {
      await assessmentHostService.deleteHostedQuestion(manageAssessment.id, question.id);
      setSuccess('Question deleted.');
      await refreshManageAssessment(manageAssessment.id);
      fetchAssessments();
    } catch (err) {
      setQuestionsError(err.response?.data?.message || 'Unable to delete question.');
    } finally {
      setBusyQuestionId(null);
    }
  };

  // ================= Add / Edit Question form =================

  const openAddQuestionForm = () => {
    const nextOrder = (manageAssessment.questions?.length || 0) + 1;
    setQuestionForm({ ...emptyQuestionForm(), orderIndex: nextOrder });
    setOriginalOptionIds([]);
    setQuestionFormErrors([]);
    setQuestionFormOpen(true);
  };

  const openEditQuestionForm = (question) => {
    const isProgramming = question.questionType === 'PROGRAMMING';
    const options = (question.options || []).map((o) => ({
      id: o.id,
      optionText: o.optionText || '',
      isCorrect: !!o.isCorrect,
    }));
    const pq = question.programmingQuestion || {};
    setQuestionForm({
      id: question.id,
      questionText: question.questionText || '',
      questionType: question.questionType || 'MCQ',
      marks: question.marks ?? 1,
      orderIndex: question.orderIndex ?? 1,
      explanation: question.explanation || '',
      options: isProgramming ? [] : (options.length > 0 ? options : [makeEmptyOption(), makeEmptyOption()]),
      programmingQuestion: {
        problemStatement: pq.problemStatement || '',
        inputFormat: pq.inputFormat || '',
        outputFormat: pq.outputFormat || '',
        constraints: pq.constraints || '',
        sampleInput: pq.sampleInput || '',
        sampleOutput: pq.sampleOutput || '',
        timeLimitMs: pq.timeLimitMs ?? 2000,
        memoryLimitMb: pq.memoryLimitMb ?? 256,
        supportedLanguages: pq.supportedLanguages || ['java', 'python', 'cpp'],
      },
    });
    setOriginalOptionIds(isProgramming ? [] : (question.options || []).map((o) => o.id));
    setQuestionFormErrors([]);
    setQuestionFormOpen(true);
  };

  const closeQuestionForm = () => {
    if (savingQuestion) return;
    setQuestionFormOpen(false);
  };

  const handleQuestionTypeChange = (type) => {
    setQuestionForm((prev) => {
      if (type === 'PROGRAMMING') {
        return { ...prev, questionType: type, options: [] };
      }
      if (type === 'TRUE_FALSE') {
        return {
          ...prev,
          questionType: type,
          options: [
            { id: null, optionText: 'True', isCorrect: false },
            { id: null, optionText: 'False', isCorrect: false },
          ],
        };
      }
      return {
        ...prev,
        questionType: type,
        options: prev.options.length >= 2 ? prev.options : [makeEmptyOption(), makeEmptyOption()],
      };
    });
  };

  const setCorrectOption = (index) => {
    setQuestionForm((prev) => ({
      ...prev,
      options: prev.options.map((o, i) => ({ ...o, isCorrect: i === index })),
    }));
  };

  const updateOptionText = (index, text) => {
    setQuestionForm((prev) => ({
      ...prev,
      options: prev.options.map((o, i) => (i === index ? { ...o, optionText: text } : o)),
    }));
  };

  const addOptionRow = () => {
    setQuestionForm((prev) => ({ ...prev, options: [...prev.options, makeEmptyOption()] }));
  };

  const removeOptionRow = (index) => {
    setQuestionForm((prev) => ({ ...prev, options: prev.options.filter((_, i) => i !== index) }));
  };

  const validateQuestionForm = (form) => {
    const errors = [];
    if (!form.questionText.trim()) errors.push('Question title/label is required.');
    if (form.marks === '' || Number(form.marks) < 0) errors.push('Marks cannot be negative.');

    if (form.questionType === 'PROGRAMMING') {
      if (!form.programmingQuestion?.problemStatement?.trim()) {
        errors.push('Problem statement is required for programming questions.');
      }
      return errors;
    }

    const filledOptions = (form.options || []).filter((o) => o.optionText.trim() !== '');
    if (filledOptions.length < 2) errors.push('At least 2 options are required.');

    const correctCount = filledOptions.filter((o) => o.isCorrect).length;
    if (correctCount === 0) errors.push('Exactly one option must be marked correct.');
    if (correctCount > 1) errors.push('Only one option can be marked correct.');

    if (form.questionType === 'TRUE_FALSE' && filledOptions.length !== 2) {
      errors.push('True/False questions must have exactly 2 options.');
    }
    return errors;
  };

  const handleQuestionSubmit = async (e) => {
    e.preventDefault();
    const errors = validateQuestionForm(questionForm);
    setQuestionFormErrors(errors);
    if (errors.length > 0) return;

    setSavingQuestion(true);
    setQuestionFormErrors([]);
    const assessmentId = manageAssessment.id;
    const isProgramming = questionForm.questionType === 'PROGRAMMING';

    const buildProgrammingPayload = () => ({
      questionText: questionForm.questionText.trim(),
      questionType: questionForm.questionType,
      marks: Number(questionForm.marks),
      orderIndex: Number(questionForm.orderIndex),
      explanation: questionForm.explanation.trim() || null,
      programmingQuestion: {
        problemStatement: questionForm.programmingQuestion.problemStatement.trim(),
        inputFormat: questionForm.programmingQuestion.inputFormat.trim() || null,
        outputFormat: questionForm.programmingQuestion.outputFormat.trim() || null,
        constraints: questionForm.programmingQuestion.constraints.trim() || null,
        sampleInput: questionForm.programmingQuestion.sampleInput.trim() || null,
        sampleOutput: questionForm.programmingQuestion.sampleOutput.trim() || null,
        timeLimitMs: Number(questionForm.programmingQuestion.timeLimitMs) || 2000,
        memoryLimitMb: Number(questionForm.programmingQuestion.memoryLimitMb) || 256,
        supportedLanguages: questionForm.programmingQuestion.supportedLanguages,
      },
    });

    try {
      if (questionForm.id) {
        const updatePayload = isProgramming
          ? buildProgrammingPayload()
          : {
              questionText: questionForm.questionText.trim(),
              questionType: questionForm.questionType,
              marks: Number(questionForm.marks),
              orderIndex: Number(questionForm.orderIndex),
              explanation: questionForm.explanation.trim() || null,
            };
        await assessmentHostService.updateHostedQuestion(assessmentId, questionForm.id, updatePayload);

        if (!isProgramming) {
          const currentIds = questionForm.options.filter((o) => o.id).map((o) => o.id);
          const removedIds = originalOptionIds.filter((id) => !currentIds.includes(id));
          for (const removedId of removedIds) {
            await assessmentHostService.deleteHostedOption(assessmentId, questionForm.id, removedId);
          }
          for (const option of questionForm.options) {
            if (!option.optionText.trim()) continue;
            const payload = { optionText: option.optionText.trim(), isCorrect: !!option.isCorrect };
            if (option.id) {
              await assessmentHostService.updateHostedOption(assessmentId, questionForm.id, option.id, payload);
            } else {
              await assessmentHostService.addHostedOption(assessmentId, questionForm.id, payload);
            }
          }
        }
        setSuccess('Question updated successfully.');
      } else {
        const addPayload = isProgramming
          ? buildProgrammingPayload()
          : {
              questionText: questionForm.questionText.trim(),
              questionType: questionForm.questionType,
              marks: Number(questionForm.marks),
              orderIndex: Number(questionForm.orderIndex),
              explanation: questionForm.explanation.trim() || null,
              options: questionForm.options
                .filter((o) => o.optionText.trim())
                .map((o) => ({ optionText: o.optionText.trim(), isCorrect: !!o.isCorrect })),
            };
        await assessmentHostService.addHostedQuestion(assessmentId, addPayload);
        setSuccess('Question added successfully.');
      }
      setQuestionFormOpen(false);
      await refreshManageAssessment(assessmentId);
      fetchAssessments();
    } catch (err) {
      setQuestionFormErrors([err.response?.data?.message || 'Unable to save question.']);
    } finally {
      setSavingQuestion(false);
    }
  };

  const sortedQuestions = (manageAssessment?.questions || []).slice().sort((a, b) => a.orderIndex - b.orderIndex);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ClipboardList size={22} color="#8b5cf6" /> My Hosted Assessments
          </h2>
          <p style={{ color: 'var(--text-muted)', margin: 0 }}>Create and manage assessments you host.</p>
        </div>
        <button onClick={openCreateAssessmentModal} className="btn btn-primary btn-sm">
          <PlusCircle size={16} />
          <span>Create Assessment</span>
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div className="alert alert-success">
          <CheckCircle size={18} />
          <span>{success}</span>
        </div>
      )}

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>Loading your assessments...</div>
        ) : assessments.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Title</th>
                  <th style={{ textAlign: 'center' }}>Duration</th>
                  <th style={{ textAlign: 'center' }}>Total Marks</th>
                  <th style={{ textAlign: 'center' }}>Questions</th>
                  <th style={{ textAlign: 'center' }}>Candidates & Attempts</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {assessments.map((assessment) => {
                  const statusStyle = STATUS_STYLES[assessment.status] || STATUS_STYLES.DRAFT;
                  const rowBusy = busyAssessmentId === assessment.id;
                  return (
                    <tr key={assessment.id}>
                      <td style={{ fontWeight: 600 }}>{assessment.title}</td>
                      <td style={{ textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)' }}>{assessment.durationMinutes} min</td>
                      <td style={{ textAlign: 'center' }}>{assessment.totalMarks}</td>
                      <td style={{ textAlign: 'center' }}>{assessment.questionCount ?? 0}</td>
                      <td style={{ textAlign: 'center' }}>
                        <span style={{
                          fontSize: '0.8rem',
                          fontWeight: 600,
                          padding: '0.2rem 0.5rem',
                          borderRadius: '6px',
                          backgroundColor: 'var(--bg-input)',
                          color: 'var(--text-muted)',
                        }}>
                          {assessment.totalAttempts ?? 0} attempts ({assessment.candidateCount ?? 0} users)
                        </span>
                      </td>
                      <td>
                        <span style={{
                          fontSize: '0.75rem', fontWeight: 700, padding: '0.2rem 0.6rem',
                          borderRadius: '9999px', color: statusStyle.color, backgroundColor: statusStyle.bg,
                        }}>
                          {statusStyle.label}
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.4rem', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                          <button
                            onClick={() => openAttemptsModal(assessment)}
                            className="btn btn-outline btn-sm"
                            disabled={rowBusy}
                            title="View Candidate Results / Submissions"
                          >
                            <Users size={14} />
                            <span>Results ({assessment.totalAttempts ?? 0})</span>
                          </button>
                          <button
                            onClick={() => openFeedbackModal(assessment)}
                            className="btn btn-outline btn-sm"
                            disabled={rowBusy}
                            title="View Candidate Feedback"
                          >
                            <MessageSquare size={14} />
                            <span>Feedback</span>
                          </button>
                          <button onClick={() => openEditAssessmentModal(assessment)} className="btn btn-outline btn-sm" disabled={rowBusy} title="Edit">
                            <Edit size={14} />
                          </button>
                          <button onClick={() => openManageQuestions(assessment)} className="btn btn-outline btn-sm" disabled={rowBusy} title="Manage Questions">
                            <ListChecks size={14} />
                            <span>Manage Questions</span>
                          </button>
                          {assessment.status === 'DRAFT' && (
                            <button onClick={() => handlePublish(assessment)} className="btn btn-outline btn-sm" disabled={rowBusy} title="Publish">
                              <Send size={14} />
                              <span>Publish</span>
                            </button>
                          )}
                          {assessment.status !== 'ARCHIVED' && (
                            <button onClick={() => handleArchive(assessment)} className="btn btn-danger btn-sm" disabled={rowBusy} title="Archive">
                              <Trash2 size={14} />
                              <span>Archive</span>
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <ClipboardList size={40} />
            <p>You haven't created any assessments yet.</p>
            <button onClick={openCreateAssessmentModal} className="btn btn-primary btn-sm" style={{ marginTop: '1rem' }}>
              Create Your First Assessment
            </button>
          </div>
        )}
      </div>

      {isAssessmentModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '620px', width: '100%', maxHeight: '92vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>
                {editingAssessmentId ? 'Edit Assessment' : 'Create New Assessment'}
              </h2>
              <button onClick={closeAssessmentModal} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {assessmentFormErrors.length > 0 && (
              <div className="alert alert-error" style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                {assessmentFormErrors.map((msg, i) => (<span key={i}>{msg}</span>))}
              </div>
            )}

            <form onSubmit={handleAssessmentSubmit}>
              <div className="form-group">
                <label>Title *</label>
                <input
                  type="text"
                  className="form-control"
                  value={assessmentForm.title}
                  onChange={(e) => setAssessmentForm({ ...assessmentForm, title: e.target.value })}
                  placeholder="e.g. JavaScript Basics Quiz"
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  rows={2}
                  className="form-control"
                  value={assessmentForm.description}
                  onChange={(e) => setAssessmentForm({ ...assessmentForm, description: e.target.value })}
                  placeholder="Briefly describe this assessment..."
                />
              </div>

              <div className="form-group">
                <label>Instructions</label>
                <textarea
                  rows={2}
                  className="form-control"
                  value={assessmentForm.instructions}
                  onChange={(e) => setAssessmentForm({ ...assessmentForm, instructions: e.target.value })}
                  placeholder="Instructions shown to the student before/during the attempt..."
                />
              </div>

              <div className="grid-cols-2">
                <div className="form-group">
                  <label>Duration (minutes) *</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control"
                    value={assessmentForm.durationMinutes}
                    onChange={(e) => setAssessmentForm({ ...assessmentForm, durationMinutes: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Passing Marks *</label>
                  <input
                    type="number"
                    min="0"
                    className="form-control"
                    value={assessmentForm.passingMarks}
                    onChange={(e) => setAssessmentForm({ ...assessmentForm, passingMarks: e.target.value })}
                  />
                </div>
              </div>

              <p style={{ fontSize: '0.78rem', color: 'var(--text-subtle)', marginTop: '-0.5rem', marginBottom: '1rem' }}>
                Total Marks isn't set here - it's calculated automatically from each question's marks once you add
                questions under "Manage Questions".
                {editingAssessmentTotalMarks != null && (<> Current total marks: <strong>{editingAssessmentTotalMarks}</strong>.</>)}
              </p>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem' }}>
                <button type="button" onClick={closeAssessmentModal} className="btn btn-outline btn-sm" disabled={savingAssessment}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary btn-sm" disabled={savingAssessment}>
                  <Save size={14} />
                  <span>{savingAssessment ? 'Saving...' : editingAssessmentId ? 'Update Assessment' : 'Create Assessment'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {manageAssessment && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '720px', width: '100%', maxHeight: '92vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div>
                <h2 style={{ fontSize: '1.3rem', fontWeight: 700 }}>{manageAssessment.title}</h2>
                <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                  <span>Duration: <strong>{manageAssessment.durationMinutes} min</strong></span>
                  <span>Total Marks: <strong>{manageAssessment.totalMarks}</strong></span>
                  <span>Passing Marks: <strong>{manageAssessment.passingMarks}</strong></span>
                </div>
              </div>
              <button onClick={closeManageQuestions} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {questionsError && (
              <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
                <AlertCircle size={16} />
                <span>{questionsError}</span>
              </div>
            )}

            {!questionFormOpen ? (
              <>
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' }}>
                  <button onClick={openAddQuestionForm} className="btn btn-primary btn-sm">
                    <PlusCircle size={14} />
                    <span>Add Question</span>
                  </button>
                </div>

                {questionsLoading ? (
                  <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '1.5rem 0' }}>Loading questions...</p>
                ) : sortedQuestions.length === 0 ? (
                  <div className="empty-state">
                    <ListChecks size={32} />
                    <p>No questions added yet.</p>
                  </div>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    {sortedQuestions.map((question, idx) => {
                      const rowBusy = busyQuestionId === question.id;
                      return (
                        <div key={question.id} style={{ border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '0.85rem 1rem' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '0.75rem' }}>
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: 700, marginBottom: '0.25rem' }}>
                                {idx + 1}. {question.questionText}
                              </div>
                              <div style={{ display: 'flex', gap: '0.6rem', fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                                <span className="badge badge-easy">{question.questionType}</span>
                                <span>{question.marks} mark{question.marks === 1 ? '' : 's'}</span>
                              </div>
                            </div>
                            <div style={{ display: 'flex', gap: '0.4rem', flexShrink: 0 }}>
                              <button onClick={() => openEditQuestionForm(question)} className="btn btn-outline btn-sm" disabled={rowBusy} title="Edit">
                                <Edit size={13} />
                              </button>
                              <button onClick={() => handleDeleteQuestion(question)} className="btn btn-danger btn-sm" disabled={rowBusy} title="Delete">
                                <Trash2 size={13} />
                              </button>
                            </div>
                          </div>

                          {question.options && question.options.length > 0 && (
                            <div style={{ marginTop: '0.6rem', display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                              {question.options.slice().sort((a, b) => a.orderIndex - b.orderIndex).map((option) => (
                                <div key={option.id} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem' }}>
                                  <span style={{
                                    width: '9px', height: '9px', borderRadius: '50%', flexShrink: 0,
                                    backgroundColor: option.isCorrect ? 'var(--success)' : 'var(--border-color)',
                                  }} />
                                  <span style={{ color: option.isCorrect ? 'var(--success)' : 'var(--text-muted)', fontWeight: option.isCorrect ? 700 : 400 }}>
                                    {option.optionText}
                                  </span>
                                </div>
                              ))}
                            </div>
                          )}

                          {question.explanation && (
                            <p style={{ marginTop: '0.5rem', fontSize: '0.78rem', color: 'var(--text-subtle)', fontStyle: 'italic' }}>
                              {question.explanation}
                            </p>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </>
            ) : (
              <form onSubmit={handleQuestionSubmit}>
                <button
                  type="button"
                  onClick={closeQuestionForm}
                  style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: '0.85rem', marginBottom: '0.75rem', padding: 0 }}
                >
                  <ArrowLeft size={14} /> Back to questions
                </button>

                <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '0.9rem' }}>
                  {questionForm.id ? 'Edit Question' : 'Add Question'}
                </h3>

                {questionFormErrors.length > 0 && (
                  <div className="alert alert-error" style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                    {questionFormErrors.map((msg, i) => (<span key={i}>{msg}</span>))}
                  </div>
                )}

                <div className="form-group">
                  <label>Question Text *</label>
                  <textarea
                    rows={2}
                    className="form-control"
                    value={questionForm.questionText}
                    onChange={(e) => setQuestionForm({ ...questionForm, questionText: e.target.value })}
                    placeholder="e.g. Which keyword declares a constant in JavaScript?"
                  />
                </div>

                <div className="grid-cols-2">
                  <div className="form-group">
                    <label>Question Type</label>
                    <select
                      className="form-control"
                      value={questionForm.questionType}
                      onChange={(e) => handleQuestionTypeChange(e.target.value)}
                    >
                      <option value="MCQ">MCQ</option>
                      <option value="TRUE_FALSE">TRUE_FALSE</option>
                      <option value="PROGRAMMING">PROGRAMMING</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Marks</label>
                    <input
                      type="number"
                      min="0"
                      className="form-control"
                      value={questionForm.marks}
                      onChange={(e) => setQuestionForm({ ...questionForm, marks: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Order</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control"
                    style={{ maxWidth: '140px' }}
                    value={questionForm.orderIndex}
                    onChange={(e) => setQuestionForm({ ...questionForm, orderIndex: e.target.value })}
                  />
                </div>

                {questionForm.questionType === 'PROGRAMMING' ? (
                  <>
                    <div className="form-group">
                      <label>Problem Statement *</label>
                      <textarea rows={5} className="form-control"
                        value={questionForm.programmingQuestion.problemStatement}
                        onChange={(e) => setQuestionForm((prev) => ({ ...prev, programmingQuestion: { ...prev.programmingQuestion, problemStatement: e.target.value } }))}
                        placeholder="Describe the problem the candidate must solve..."
                      />
                    </div>
                    <div className="grid-cols-2">
                      <div className="form-group">
                        <label>Input Format</label>
                        <textarea rows={2} className="form-control"
                          value={questionForm.programmingQuestion.inputFormat}
                          onChange={(e) => setQuestionForm((prev) => ({ ...prev, programmingQuestion: { ...prev.programmingQuestion, inputFormat: e.target.value } }))}
                          placeholder="Describe the input format..."
                        />
                      </div>
                      <div className="form-group">
                        <label>Output Format</label>
                        <textarea rows={2} className="form-control"
                          value={questionForm.programmingQuestion.outputFormat}
                          onChange={(e) => setQuestionForm((prev) => ({ ...prev, programmingQuestion: { ...prev.programmingQuestion, outputFormat: e.target.value } }))}
                          placeholder="Describe the expected output format..."
                        />
                      </div>
                    </div>
                    <div className="form-group">
                      <label>Constraints</label>
                      <textarea rows={2} className="form-control"
                        value={questionForm.programmingQuestion.constraints}
                        onChange={(e) => setQuestionForm((prev) => ({ ...prev, programmingQuestion: { ...prev.programmingQuestion, constraints: e.target.value } }))}
                        placeholder="e.g. 1 ≤ N ≤ 10^5"
                      />
                    </div>
                    <div className="grid-cols-2">
                      <div className="form-group">
                        <label>Sample Input</label>
                        <textarea rows={3} className="form-control" style={{ fontFamily: 'monospace', fontSize: '0.85rem' }}
                          value={questionForm.programmingQuestion.sampleInput}
                          onChange={(e) => setQuestionForm((prev) => ({ ...prev, programmingQuestion: { ...prev.programmingQuestion, sampleInput: e.target.value } }))}
                          placeholder="Sample input shown to candidate"
                        />
                      </div>
                      <div className="form-group">
                        <label>Sample Output</label>
                        <textarea rows={3} className="form-control" style={{ fontFamily: 'monospace', fontSize: '0.85rem' }}
                          value={questionForm.programmingQuestion.sampleOutput}
                          onChange={(e) => setQuestionForm((prev) => ({ ...prev, programmingQuestion: { ...prev.programmingQuestion, sampleOutput: e.target.value } }))}
                          placeholder="Expected output for sample"
                        />
                      </div>
                    </div>
                    <div className="grid-cols-2">
                      <div className="form-group">
                        <label>Time Limit (ms)</label>
                        <input type="number" min="500" max="10000" className="form-control"
                          value={questionForm.programmingQuestion.timeLimitMs}
                          onChange={(e) => setQuestionForm((prev) => ({ ...prev, programmingQuestion: { ...prev.programmingQuestion, timeLimitMs: e.target.value } }))}
                        />
                      </div>
                      <div className="form-group">
                        <label>Memory Limit (MB)</label>
                        <input type="number" min="32" max="512" className="form-control"
                          value={questionForm.programmingQuestion.memoryLimitMb}
                          onChange={(e) => setQuestionForm((prev) => ({ ...prev, programmingQuestion: { ...prev.programmingQuestion, memoryLimitMb: e.target.value } }))}
                        />
                      </div>
                    </div>
                    <div className="form-group">
                      <label>Supported Languages</label>
                      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginTop: '0.35rem' }}>
                        {['java', 'python', 'cpp', 'javascript', 'c'].map((lang) => {
                          const checked = questionForm.programmingQuestion.supportedLanguages.includes(lang);
                          return (
                            <label key={lang} style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', cursor: 'pointer', fontSize: '0.9rem' }}>
                              <input type="checkbox" checked={checked} onChange={() => {
                                setQuestionForm((prev) => ({
                                  ...prev,
                                  programmingQuestion: {
                                    ...prev.programmingQuestion,
                                    supportedLanguages: checked ? prev.programmingQuestion.supportedLanguages.filter((l) => l !== lang) : [...prev.programmingQuestion.supportedLanguages, lang],
                                  },
                                }));
                              }} />
                              {lang}
                            </label>
                          );
                        })}
                      </div>
                      <p style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', marginTop: '0.3rem' }}>
                        Test cases are managed after saving this question (Edit → Test Cases).
                      </p>
                    </div>
                    <div className="form-group">
                      <label>Explanation (optional)</label>
                      <textarea rows={2} className="form-control"
                        value={questionForm.explanation}
                        onChange={(e) => setQuestionForm({ ...questionForm, explanation: e.target.value })}
                      />
                    </div>
                  </>
                ) : (
                  <>
                    <div className="form-group">
                      <label>Explanation (optional, shown after submission)</label>
                      <textarea rows={2} className="form-control"
                        value={questionForm.explanation}
                        onChange={(e) => setQuestionForm({ ...questionForm, explanation: e.target.value })}
                      />
                    </div>
                    <div className="form-group">
                      <label>Options - mark exactly one as correct *</label>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        {questionForm.options.map((option, idx) => (
                          <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <input type="radio" name="correct-option" checked={!!option.isCorrect} onChange={() => setCorrectOption(idx)} title="Mark as correct" />
                            <input type="text" className="form-control" value={option.optionText}
                              onChange={(e) => updateOptionText(idx, e.target.value)}
                              placeholder={`Option ${idx + 1}`}
                              disabled={questionForm.questionType === 'TRUE_FALSE'}
                            />
                            {questionForm.questionType !== 'TRUE_FALSE' && (
                              <button type="button" onClick={() => removeOptionRow(idx)} className="btn btn-danger btn-sm"
                                disabled={questionForm.options.length <= 2} title="Remove option">
                                <Trash2 size={13} />
                              </button>
                            )}
                          </div>
                        ))}
                      </div>
                      {questionForm.questionType !== 'TRUE_FALSE' && (
                        <button type="button" onClick={addOptionRow} className="btn btn-outline btn-sm" style={{ marginTop: '0.6rem' }}>
                          <PlusCircle size={13} />
                          <span>Add Option</span>
                        </button>
                      )}
                    </div>
                  </>
                )}

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem' }}>
                  <button type="button" onClick={closeQuestionForm} className="btn btn-outline btn-sm" disabled={savingQuestion}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary btn-sm" disabled={savingQuestion}>
                    <Save size={14} />
                    <span>{savingQuestion ? 'Saving...' : questionForm.id ? 'Update Question' : 'Add Question'}</span>
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* Candidate Attempts Modal */}
      {candidateAttemptsModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '900px', width: '100%', maxHeight: '92vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
              <div>
                <h2 style={{ fontSize: '1.35rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Users size={22} color="var(--primary)" /> Candidate Results & Submissions
                </h2>
                <p style={{ color: 'var(--text-muted)', margin: '0.2rem 0 0', fontSize: '0.9rem' }}>
                  Assessment: <strong>{selectedAssessmentForAttempts?.title}</strong> (Pass mark: {selectedAssessmentForAttempts?.passingMarks}/{selectedAssessmentForAttempts?.totalMarks})
                </p>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                {attemptsList.length > 0 && (
                  <button onClick={exportAttemptsCSV} className="btn btn-outline btn-sm" title="Export to CSV">
                    <Download size={14} />
                    <span>Export CSV</span>
                  </button>
                )}
                <button onClick={closeAttemptsModal} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                  <X size={20} />
                </button>
              </div>
            </div>

            {attemptsError && (
              <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
                <AlertCircle size={18} />
                <span>{attemptsError}</span>
              </div>
            )}

            {loadingAttempts ? (
              <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
                Loading candidate attempts...
              </div>
            ) : attemptsList.length === 0 ? (
              <div className="empty-state" style={{ padding: '2.5rem 0' }}>
                <Users size={36} />
                <p>No candidates have taken this assessment yet.</p>
              </div>
            ) : (
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Candidate</th>
                      <th>Email</th>
                      <th style={{ textAlign: 'center' }}>Score</th>
                      <th style={{ textAlign: 'center' }}>Status</th>
                      <th style={{ textAlign: 'center' }}>Result</th>
                      <th style={{ textAlign: 'right' }}>Submitted At</th>
                    </tr>
                  </thead>
                  <tbody>
                    {attemptsList.map((att) => {
                      const isPassed = att.passed === true;
                      const isFailed = att.passed === false;
                      return (
                        <tr key={att.id}>
                          <td>
                            <div style={{ fontWeight: 600 }}>{att.candidateFullName || att.candidateUsername || 'Candidate'}</div>
                            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>@{att.candidateUsername || 'user'}</div>
                          </td>
                          <td style={{ fontSize: '0.85rem' }}>{att.candidateEmail || '-'}</td>
                          <td style={{ textAlign: 'center', fontWeight: 700 }}>
                            {att.score ?? '-'}{att.totalMarks != null ? ` / ${att.totalMarks}` : ''}
                          </td>
                          <td style={{ textAlign: 'center' }}>
                            <span style={{
                              fontSize: '0.75rem',
                              fontWeight: 600,
                              padding: '0.2rem 0.5rem',
                              borderRadius: '4px',
                              backgroundColor: att.status === 'COMPLETED' ? 'rgba(34, 197, 94, 0.12)' : 'rgba(245, 158, 11, 0.12)',
                              color: att.status === 'COMPLETED' ? '#15803d' : '#b45309',
                            }}>
                              {att.status}
                            </span>
                          </td>
                          <td style={{ textAlign: 'center' }}>
                            {isPassed && (
                              <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#15803d', backgroundColor: 'rgba(34, 197, 94, 0.15)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>
                                PASSED
                              </span>
                            )}
                            {isFailed && (
                              <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--danger)', backgroundColor: 'rgba(239, 68, 68, 0.12)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>
                                FAILED
                              </span>
                            )}
                            {!isPassed && !isFailed && (
                              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                                IN PROGRESS
                              </span>
                            )}
                          </td>
                          <td style={{ textAlign: 'right', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                            {att.completedAt ? new Date(att.completedAt).toLocaleString() : (att.startedAt ? new Date(att.startedAt).toLocaleString() : '-')}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Host Candidate Feedback Modal */}
      {feedbackModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '840px', width: '100%', maxHeight: '92vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
              <div>
                <h2 style={{ fontSize: '1.35rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
                  <MessageSquare size={22} color="var(--primary)" /> Candidate Assessment Feedback
                </h2>
                <p style={{ color: 'var(--text-muted)', margin: '0.2rem 0 0', fontSize: '0.9rem' }}>
                  Assessment: <strong>{selectedAssessmentForFeedback?.title}</strong>
                </p>
              </div>
              <button onClick={closeFeedbackModal} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {feedbackError && (
              <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
                <AlertCircle size={18} />
                <span>{feedbackError}</span>
              </div>
            )}

            {/* Feedback Metrics Summary Banner */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1.25rem', padding: '1rem', backgroundColor: 'var(--bg-input)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
              <div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-subtle)', fontWeight: 600, textTransform: 'uppercase' }}>Total Feedback Received</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--primary)' }}>{feedbackSummary?.totalFeedback ?? 0}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-subtle)', fontWeight: 600, textTransform: 'uppercase' }}>Average Rating</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.2rem' }}>
                  <span style={{ fontSize: '1.5rem', fontWeight: 800 }}>{feedbackSummary?.averageRating ? feedbackSummary.averageRating.toFixed(1) : '—'}</span>
                  {feedbackSummary?.averageRating != null && feedbackSummary.averageRating > 0 && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.15rem' }}>
                      {[1, 2, 3, 4, 5].map((star) => (
                        <Star
                          key={star}
                          size={16}
                          color={feedbackSummary.averageRating >= star ? '#f59e0b' : 'var(--text-subtle)'}
                          fill={feedbackSummary.averageRating >= star ? '#f59e0b' : 'transparent'}
                        />
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>

            {loadingFeedback ? (
              <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
                Loading assessment feedback...
              </div>
            ) : !feedbackSummary?.feedbacks || feedbackSummary.feedbacks.length === 0 ? (
              <div className="empty-state" style={{ padding: '2.5rem 0' }}>
                <MessageSquare size={36} />
                <p>No post-completion feedback received for this assessment yet.</p>
              </div>
            ) : (
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Candidate</th>
                      <th style={{ textAlign: 'center' }}>Rating</th>
                      <th>Feedback Type</th>
                      <th>Comments</th>
                      <th style={{ textAlign: 'right' }}>Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {feedbackSummary.feedbacks.map((fb) => (
                      <tr key={fb.id}>
                        <td>
                          <div style={{ fontWeight: 600 }}>@{fb.username || 'candidate'}</div>
                          <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>{fb.userEmail || ''}</div>
                        </td>
                        <td style={{ textAlign: 'center', whiteSpace: 'nowrap' }}>
                          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}>
                            {[1, 2, 3, 4, 5].map((star) => (
                              <Star
                                key={star}
                                size={14}
                                color={fb.rating >= star ? '#f59e0b' : 'var(--border-color)'}
                                fill={fb.rating >= star ? '#f59e0b' : 'transparent'}
                              />
                            ))}
                            <span style={{ fontSize: '0.8rem', fontWeight: 600, marginLeft: '0.25rem' }}>({fb.rating}/5)</span>
                          </div>
                        </td>
                        <td>
                          <span style={{
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            padding: '0.2rem 0.6rem',
                            borderRadius: '9999px',
                            backgroundColor: 'var(--primary-soft)',
                            color: 'var(--primary)',
                            whiteSpace: 'nowrap',
                          }}>
                            {fb.feedbackType}
                          </span>
                        </td>
                        <td style={{ maxWidth: '320px', fontSize: '0.85rem', color: 'var(--text-main)', whiteSpace: 'pre-wrap' }}>
                          {fb.comments || <span style={{ color: 'var(--text-subtle)', fontStyle: 'italic' }}>No comments provided</span>}
                        </td>
                        <td style={{ textAlign: 'right', fontSize: '0.8rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                          {fb.createdAt ? new Date(fb.createdAt).toLocaleDateString() : '—'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default HostAssessment;

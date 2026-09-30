import React, { useState } from 'react';
import { MessageSquare, Star, Send, CheckCircle2, AlertCircle, Sparkles } from 'lucide-react';
import supportFeedbackService from '../../services/supportFeedbackService';

const FEEDBACK_TYPES = [
  'Suggestion',
  'Feature Request',
  'General Feedback',
  'Other'
];

const RATING_LABELS = {
  1: 'Poor',
  2: 'Fair',
  3: 'Good',
  4: 'Very Good',
  5: 'Excellent'
};

const Feedback = () => {
  const [formData, setFormData] = useState({
    feedbackType: FEEDBACK_TYPES[0],
    rating: 5,
    message: ''
  });
  const [hoverRating, setHoverRating] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.message.trim()) {
      setError('Please provide your feedback message.');
      return;
    }

    try {
      setLoading(true);
      setError('');
      setSubmitted(false);
      await supportFeedbackService.createGeneralFeedback(formData);
      setSubmitted(true);
      setFormData({
        feedbackType: FEEDBACK_TYPES[0],
        rating: 5,
        message: ''
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit feedback. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '760px', margin: '0 auto' }}>
      {/* Page Header */}
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <MessageSquare size={28} color="var(--primary)" /> Feedback
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Help us improve CodeNova by sharing your thoughts and experience.
        </p>
      </div>

      {/* Success Alert */}
      {submitted && (
        <div className="alert alert-success" style={{ marginBottom: '1.5rem' }}>
          <CheckCircle2 size={20} />
          <div>
            <strong>Thank you for your feedback!</strong> Your insights help us continually improve CodeNova for the developer community.
          </div>
        </div>
      )}

      {/* Error Alert */}
      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1.5rem' }}>
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {/* Feedback Submission Card */}
      <div className="card">
        <h2 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Sparkles size={20} color="var(--primary)" /> Share Your Experience
        </h2>

        <form onSubmit={handleSubmit}>
          {/* Feedback Type Buttons */}
          <div className="form-group">
            <label>Feedback Type</label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.35rem' }}>
              {FEEDBACK_TYPES.map((type) => {
                const isSelected = formData.feedbackType === type;
                return (
                  <button
                    key={type}
                    type="button"
                    onClick={() => setFormData({ ...formData, feedbackType: type })}
                    className={`btn btn-sm ${isSelected ? 'btn-primary' : 'btn-outline'}`}
                    style={{ fontWeight: 600 }}
                  >
                    {type}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Interactive Star Rating */}
          <div className="form-group">
            <label>Overall Experience</label>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '0.35rem' }}>
              <div
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.35rem',
                  background: 'var(--bg-main)',
                  padding: '0.4rem 0.75rem',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-color)',
                }}
              >
                {[1, 2, 3, 4, 5].map((star) => {
                  const active = (hoverRating || formData.rating) >= star;
                  return (
                    <button
                      key={star}
                      type="button"
                      onMouseEnter={() => setHoverRating(star)}
                      onMouseLeave={() => setHoverRating(0)}
                      onClick={() => setFormData({ ...formData, rating: star })}
                      style={{
                        background: 'transparent',
                        border: 'none',
                        cursor: 'pointer',
                        padding: '0.2rem',
                        display: 'flex',
                        alignItems: 'center',
                      }}
                      title={`${star} Star${star > 1 ? 's' : ''} - ${RATING_LABELS[star]}`}
                    >
                      <Star
                        size={24}
                        color={active ? '#f59e0b' : 'var(--text-subtle)'}
                        fill={active ? '#f59e0b' : 'transparent'}
                        style={{ transition: 'transform 0.15s ease, fill 0.15s ease' }}
                      />
                    </button>
                  );
                })}
              </div>
              <span style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--text-main)' }}>
                {RATING_LABELS[hoverRating || formData.rating]} ({hoverRating || formData.rating}/5)
              </span>
            </div>
          </div>

          {/* Message Textarea */}
          <div className="form-group">
            <label htmlFor="feedback-message">Your Message</label>
            <textarea
              id="feedback-message"
              className="form-control"
              rows={6}
              placeholder="Tell us what you like, what was confusing, or what features you'd like to see next..."
              value={formData.message}
              onChange={(e) => setFormData({ ...formData, message: e.target.value })}
              required
            />
          </div>

          {/* Submit Button */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.25rem' }}>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              <Send size={16} />
              <span>{loading ? 'Sending...' : 'Send Feedback'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Feedback;

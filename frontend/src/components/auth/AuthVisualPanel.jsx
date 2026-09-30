import React from 'react';
import { Link } from 'react-router-dom';
import Logo from '../Logo';
import {
  Code2,
  Trophy,
  ShieldCheck,
  Zap,
  Sparkles,
  Terminal,
  CheckCircle2,
  Cpu,
  Layers
} from 'lucide-react';

const AuthVisualPanel = () => {
  return (
    <div className="auth-visual-panel">
      <style>{`
        .auth-visual-panel {
          height: 100%;
          min-height: 100vh;
          background: linear-gradient(145deg, #f8faff 0%, #f0f4ff 50%, #faf5ff 100%);
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          padding: 2.5rem 3rem;
          position: relative;
          overflow: hidden;
          border-right: 1px solid rgba(124, 58, 237, 0.08);
        }

        /* Decorative background elements */
        .auth-bg-circle-1 {
          position: absolute;
          top: -80px;
          right: -80px;
          width: 280px;
          height: 280px;
          border-radius: 50%;
          background: radial-gradient(circle, rgba(124, 58, 237, 0.12) 0%, rgba(124, 58, 237, 0) 70%);
          pointer-events: none;
        }
        .auth-bg-circle-2 {
          position: absolute;
          bottom: -60px;
          left: -60px;
          width: 240px;
          height: 240px;
          border-radius: 50%;
          background: radial-gradient(circle, rgba(59, 130, 246, 0.1) 0%, rgba(59, 130, 246, 0) 70%);
          pointer-events: none;
        }

        .auth-brand-header {
          display: flex;
          align-items: center;
          gap: 0.75rem;
          z-index: 2;
        }

        .auth-brand-badge {
          display: inline-flex;
          align-items: center;
          gap: 0.4rem;
          padding: 0.3rem 0.75rem;
          border-radius: 9999px;
          background: rgba(124, 58, 237, 0.1);
          color: #6d28d9;
          font-size: 0.725rem;
          font-weight: 700;
          letter-spacing: 0.05em;
          text-transform: uppercase;
        }

        .auth-hero-section {
          margin: 1.5rem 0 1rem;
          z-index: 2;
        }

        .auth-main-heading {
          font-size: 2.25rem;
          font-weight: 800;
          color: #1e1b4b;
          line-height: 1.2;
          margin: 0.75rem 0 0.5rem;
          letter-spacing: -0.02em;
        }

        .auth-description {
          font-size: 0.95rem;
          color: #475569;
          line-height: 1.6;
          max-width: 520px;
          margin-bottom: 1.5rem;
        }

        /* Modern Developer Illustration */
        .auth-illustration-container {
          position: relative;
          width: 100%;
          max-width: 480px;
          margin: 0 auto 1.5rem;
          display: flex;
          justify-content: center;
          align-items: center;
          z-index: 2;
        }

        .auth-illustration-svg {
          width: 100%;
          height: auto;
          filter: drop-shadow(0 12px 24px rgba(124, 58, 237, 0.08));
        }

        /* 4 Compact Benefits */
        .auth-benefits-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 0.85rem;
          z-index: 2;
          margin-top: auto;
        }

        .auth-benefit-card {
          display: flex;
          align-items: flex-start;
          gap: 0.75rem;
          background: rgba(255, 255, 255, 0.75);
          backdrop-filter: blur(8px);
          padding: 0.75rem 0.9rem;
          border-radius: 10px;
          border: 1px solid rgba(226, 232, 240, 0.8);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
        }

        .auth-benefit-icon-wrapper {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }

        .auth-benefit-title {
          font-size: 0.825rem;
          font-weight: 700;
          color: #1e293b;
          margin: 0 0 0.15rem;
        }

        .auth-benefit-desc {
          font-size: 0.75rem;
          color: #64748b;
          margin: 0;
          line-height: 1.35;
        }

        @media (max-width: 1200px) {
          .auth-visual-panel {
            padding: 2rem;
          }
          .auth-main-heading {
            font-size: 1.9rem;
          }
          .auth-benefits-grid {
            grid-template-columns: 1fr;
            gap: 0.6rem;
          }
        }

        @media (max-width: 900px) {
          .auth-visual-panel {
            min-height: auto;
            padding: 2rem 1.5rem;
            border-right: none;
            border-bottom: 1px solid rgba(124, 58, 237, 0.08);
          }
          .auth-illustration-container {
            max-width: 320px;
            margin: 1rem auto;
          }
          .auth-benefits-grid {
            display: none;
          }
        }
      `}</style>

      {/* Decorative circles */}
      <div className="auth-bg-circle-1" />
      <div className="auth-bg-circle-2" />

      {/* Top Branding */}
      <div>
        <div className="auth-brand-header">
          <Link to="/" style={{ display: 'inline-flex', alignItems: 'center', textDecoration: 'none' }}>
            <Logo size={36} withWordmark />
          </Link>
        </div>

        <div className="auth-hero-section">
          <div className="auth-brand-badge">
            <Sparkles size={13} /> NEXT-GEN DEVELOPER PRACTICE PLATFORM
          </div>

          <h1 className="auth-main-heading">
            Code. Practice. Compete. Grow.
          </h1>

          <p className="auth-description">
            CodeNova provides a modern coding environment where developers can practice programming, participate in contests, complete assessments, earn verified certificates and improve their skills.
          </p>
        </div>
      </div>

      {/* Large Modern Developer Illustration */}
      <div className="auth-illustration-container">
        <svg
          className="auth-illustration-svg"
          viewBox="0 0 520 320"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          {/* Subtle grid pattern background */}
          <pattern id="code-grid" width="20" height="20" patternUnits="userSpaceOnUse">
            <path d="M 20 0 L 0 0 0 20" fill="none" stroke="rgba(124, 58, 237, 0.04)" strokeWidth="1" />
          </pattern>
          <rect width="520" height="320" rx="16" fill="url(#code-grid)" />

          {/* Glowing backdrop shadow */}
          <ellipse cx="260" cy="270" rx="190" ry="24" fill="rgba(124, 58, 237, 0.08)" />

          {/* Modern Code Editor Window (Laptop Screen / Window) */}
          <g filter="drop-shadow(0 8px 20px rgba(99, 102, 241, 0.12))">
            {/* Editor Base */}
            <rect x="70" y="35" width="380" height="235" rx="12" fill="#1e1b4b" />
            <rect x="70" y="35" width="380" height="32" rx="12" fill="#2e1065" />
            
            {/* Window control dots */}
            <circle cx="92" cy="51" r="5" fill="#ef4444" />
            <circle cx="108" cy="51" r="5" fill="#f59e0b" />
            <circle cx="124" cy="51" r="5" fill="#10b981" />

            {/* Active Tab */}
            <rect x="145" y="41" width="110" height="26" rx="6" fill="#3b0764" />
            <text x="165" y="58" fill="#e9d5ff" fontSize="11" fontWeight="600" fontFamily="monospace">Solution.java</text>
            <circle cx="245" cy="54" r="3" fill="#a855f7" />

            {/* Editor Sidebar / Line Numbers */}
            <rect x="70" y="67" width="36" height="203" fill="#17143a" />
            <text x="86" y="92" fill="#6b7280" fontSize="10" fontFamily="monospace" textAnchor="middle">1</text>
            <text x="86" y="112" fill="#6b7280" fontSize="10" fontFamily="monospace" textAnchor="middle">2</text>
            <text x="86" y="132" fill="#6b7280" fontSize="10" fontFamily="monospace" textAnchor="middle">3</text>
            <text x="86" y="152" fill="#6b7280" fontSize="10" fontFamily="monospace" textAnchor="middle">4</text>
            <text x="86" y="172" fill="#6b7280" fontSize="10" fontFamily="monospace" textAnchor="middle">5</text>
            <text x="86" y="192" fill="#6b7280" fontSize="10" fontFamily="monospace" textAnchor="middle">6</text>
            <text x="86" y="212" fill="#6b7280" fontSize="10" fontFamily="monospace" textAnchor="middle">7</text>

            {/* Code Lines with Syntax Colors */}
            <text x="120" y="92" fill="#f43f5e" fontSize="11" fontWeight="700" fontFamily="monospace">class</text>
            <text x="160" y="92" fill="#38bdf8" fontSize="11" fontWeight="700" fontFamily="monospace">Solution</text>
            <text x="215" y="92" fill="#cbd5e1" fontSize="11" fontFamily="monospace">{'{'}</text>

            <text x="135" y="112" fill="#f43f5e" fontSize="11" fontWeight="700" fontFamily="monospace">public int[]</text>
            <text x="220" y="112" fill="#a78bfa" fontSize="11" fontWeight="600" fontFamily="monospace">twoSum</text>
            <text x="268" y="112" fill="#cbd5e1" fontSize="11" fontFamily="monospace">(int[] nums, int target) {'{'}</text>

            <text x="150" y="132" fill="#34d399" fontSize="11" fontFamily="monospace">Map&lt;Integer, Integer&gt; map = new HashMap&lt;&gt;();</text>
            <text x="150" y="152" fill="#f43f5e" fontSize="11" fontWeight="700" fontFamily="monospace">for</text>
            <text x="175" y="152" fill="#cbd5e1" fontSize="11" fontFamily="monospace">(int i = 0; i &lt; nums.length; i++) {'{'}</text>
            <text x="165" y="172" fill="#a78bfa" fontSize="11" fontFamily="monospace">if (map.containsKey(target - nums[i]))</text>
            <text x="180" y="192" fill="#38bdf8" fontSize="11" fontWeight="700" fontFamily="monospace">return new int[]</text>
            <text x="290" y="192" fill="#fbbf24" fontSize="11" fontFamily="monospace">{'{ map.get(target - nums[i]), i };'}</text>
            <text x="150" y="212" fill="#cbd5e1" fontSize="11" fontFamily="monospace">{'}'}</text>

            {/* Test Passed Badge */}
            <g transform="translate(270, 215)">
              <rect width="165" height="38" rx="8" fill="#065f46" fillOpacity="0.9" stroke="#10b981" strokeWidth="1.5" />
              <circle cx="20" cy="19" r="8" fill="#10b981" />
              <path d="M16 19 L19 22 L25 15" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              <text x="36" y="18" fill="#ecfdf5" fontSize="10" fontWeight="700">All Tests Passed</text>
              <text x="36" y="29" fill="#a7f3d0" fontSize="9">Runtime: 1ms • Beats 99.8%</text>
            </g>
          </g>

          {/* Floating Language Badge: Java */}
          <g transform="translate(42, 75)" filter="drop-shadow(0 4px 12px rgba(0,0,0,0.08))">
            <rect width="52" height="48" rx="10" fill="#ffffff" stroke="#e2e8f0" strokeWidth="1" />
            <rect x="8" y="8" width="36" height="32" rx="6" fill="#ea580c" fillOpacity="0.1" />
            <text x="26" y="28" fill="#ea580c" fontSize="12" fontWeight="800" textAnchor="middle">JAVA</text>
          </g>

          {/* Floating Language Badge: Python */}
          <g transform="translate(435, 65)" filter="drop-shadow(0 4px 12px rgba(0,0,0,0.08))">
            <rect width="52" height="48" rx="10" fill="#ffffff" stroke="#e2e8f0" strokeWidth="1" />
            <rect x="8" y="8" width="36" height="32" rx="6" fill="#3b82f6" fillOpacity="0.1" />
            <text x="26" y="28" fill="#2563eb" fontSize="12" fontWeight="800" textAnchor="middle">PY</text>
          </g>

          {/* Floating Language Badge: C++ */}
          <g transform="translate(425, 175)" filter="drop-shadow(0 4px 12px rgba(0,0,0,0.08))">
            <rect width="52" height="48" rx="10" fill="#ffffff" stroke="#e2e8f0" strokeWidth="1" />
            <rect x="8" y="8" width="36" height="32" rx="6" fill="#8b5cf6" fillOpacity="0.1" />
            <text x="26" y="28" fill="#7c3aed" fontSize="12" fontWeight="800" textAnchor="middle">C++</text>
          </g>
        </svg>
      </div>

      {/* 4 Compact Benefits Grid */}
      <div className="auth-benefits-grid">
        <div className="auth-benefit-card">
          <div className="auth-benefit-icon-wrapper" style={{ backgroundColor: 'rgba(124, 58, 237, 0.1)', color: '#7c3aed' }}>
            <Code2 size={18} />
          </div>
          <div>
            <h4 className="auth-benefit-title">Interactive Coding Practice</h4>
            <p className="auth-benefit-desc">Practice problems across multiple programming languages.</p>
          </div>
        </div>

        <div className="auth-benefit-card">
          <div className="auth-benefit-icon-wrapper" style={{ backgroundColor: 'rgba(22, 163, 74, 0.1)', color: '#16a34a' }}>
            <Trophy size={18} />
          </div>
          <div>
            <h4 className="auth-benefit-title">Competitive Contests</h4>
            <p className="auth-benefit-desc">Compete with other developers and track your progress.</p>
          </div>
        </div>

        <div className="auth-benefit-card">
          <div className="auth-benefit-icon-wrapper" style={{ backgroundColor: 'rgba(59, 130, 246, 0.1)', color: '#2563eb' }}>
            <Zap size={18} />
          </div>
          <div>
            <h4 className="auth-benefit-title">Skill Assessments</h4>
            <p className="auth-benefit-desc">Take structured assessments with detailed feedback.</p>
          </div>
        </div>

        <div className="auth-benefit-card">
          <div className="auth-benefit-icon-wrapper" style={{ backgroundColor: 'rgba(217, 119, 6, 0.1)', color: '#d97706' }}>
            <ShieldCheck size={18} />
          </div>
          <div>
            <h4 className="auth-benefit-title">Verified Certificates</h4>
            <p className="auth-benefit-desc">Earn certificates for your coding achievements.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthVisualPanel;

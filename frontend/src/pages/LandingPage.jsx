import React from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Logo from '../components/Logo';
import {
  Code2,
  ArrowRight,
  CheckCircle2,
  Shield,
  Terminal,
  Trophy,
  Zap,
  BarChart3,
  Award
} from 'lucide-react';

const LandingPage = () => {
  const { t } = useTranslation();

  return (
    <div className="landing-page">

      {/* HERO */}
      <section className="landing-hero">
        <div className="hero-content">

          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
            <Logo size={40} withWordmark />
          </div>

          <div className="hero-badge">
            <span className="hero-badge-dot"></span>
            {t('landing.tagline', 'Learn. Code. Solve. Achieve.')}
          </div>

          <h1>
            {t('landing.headlinePart1', 'Practice.')}
            <span>{t('landing.headlinePart2', ' Code.')}</span>
            <br />
            <strong>{t('landing.headlinePart3', 'Improve.')}</strong>
          </h1>

          <p className="hero-description">
            {t('landing.description', 'Build your programming skills with real coding problems, automated test cases, submission tracking, and progress analytics.')}
          </p>

          <div className="hero-actions">
            <Link to="/register" className="btn btn-primary hero-btn">
              {t('landing.startCoding', 'Start Coding')}
              <ArrowRight size={18} />
            </Link>

            <Link to="/login" className="btn btn-outline hero-btn">
              {t('landing.signIn', 'Sign In')}
            </Link>
          </div>

          <div className="hero-stats">
            <div>
              <strong>100+</strong>
              <span>{t('landing.problemsStat', 'Problems')}</span>
            </div>

            <div>
              <strong>3</strong>
              <span>{t('landing.difficultyStat', 'Difficulty Levels')}</span>
            </div>

            <div>
              <strong>24/7</strong>
              <span>{t('landing.practiceStat', 'Practice')}</span>
            </div>
          </div>
        </div>

        {/* MOCK EDITOR */}
        <div className="mock-editor">

          <div className="mock-editor-header">
            <div className="editor-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>

            <span>Solution.java</span>
          </div>

          <div className="mock-editor-body">
            <div className="code-line">
              <span className="line-number">1</span>
              <span>
                <span className="code-keyword">class</span>{' '}
                <span className="code-name">Solution</span> {'{'}
              </span>
            </div>

            <div className="code-line">
              <span className="line-number">2</span>
              <span className="indent">
                <span className="code-keyword">public</span>{' '}
                <span className="code-type">boolean</span> isPalindrome(
                <span className="code-type">int</span> x) {'{'}
              </span>
            </div>

            <div className="code-line">
              <span className="line-number">3</span>
              <span className="indent-2">
                <span className="code-keyword">return</span> x ==
                reverse(x);
              </span>
            </div>

            <div className="code-line">
              <span className="line-number">4</span>
              <span className="indent">{'}'}</span>
            </div>

            <div className="code-line">
              <span className="line-number">5</span>
              <span>{'}'}</span>
            </div>
          </div>

          <div className="mock-editor-footer">
            <span>
              <CheckCircle2 size={15} />
              All tests passed
            </span>

            <span>Runtime: 24ms</span>
          </div>
        </div>
      </section>

      {/* FEATURES */}
      <section className="landing-section">

        <div className="section-heading">
          <span className="section-eyebrow">WHY CODEPLATFORM</span>

          <h2>
            Everything you need to
            <span> practice better.</span>
          </h2>

          <p>
            A focused environment designed to help students
            learn, practice, and measure their coding progress.
          </p>
        </div>

        <div className="feature-grid">

          <div className="feature-card">
            <div className="feature-icon">
              <Code2 size={22} />
            </div>

            <h3>Practice Problems</h3>

            <p>
              Solve carefully designed coding problems across
              different difficulty levels.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              <Terminal size={22} />
            </div>

            <h3>Online Code Editor</h3>

            <p>
              Write and execute your solutions directly inside
              the platform.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              <Zap size={22} />
            </div>

            <h3>Automated Testing</h3>

            <p>
              Every submission is evaluated against predefined
              test cases automatically.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              <BarChart3 size={22} />
            </div>

            <h3>Track Progress</h3>

            <p>
              Monitor solved problems, submissions, and your
              overall coding progress.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              <Trophy size={22} />
            </div>

            <h3>Leaderboard</h3>

            <p>
              See how you rank globally, weekly, or by language
              based on real submission data.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              <Award size={22} />
            </div>

            <h3>Certificates</h3>

            <p>
              Earn a verifiable certificate every 50 problems
              you solve, downloadable as a PDF.
            </p>
          </div>

        </div>
      </section>

      {/* HOW IT WORKS */}
      <section className="steps-section">

        <div className="section-heading">
          <span className="section-eyebrow">HOW IT WORKS</span>

          <h2>
            Start coding in
            <span> three simple steps.</span>
          </h2>
        </div>

        <div className="steps-grid">

          <div className="step-card">
            <div className="step-number">01</div>

            <h3>Create an account</h3>

            <p>
              Register and create your coding practice profile.
            </p>
          </div>

          <div className="step-card">
            <div className="step-number">02</div>

            <h3>Choose a problem</h3>

            <p>
              Pick a problem based on your preferred difficulty
              and topic.
            </p>
          </div>

          <div className="step-card">
            <div className="step-number">03</div>

            <h3>Submit your solution</h3>

            <p>
              Run your code, pass the test cases, and track your
              progress.
            </p>
          </div>

        </div>
      </section>

      {/* CTA */}
      <section className="landing-cta">

        <div className="cta-icon">
          <Trophy size={26} />
        </div>

        <h2>Ready to improve your coding skills?</h2>

        <p>
          Start solving problems and build your confidence one
          solution at a time.
        </p>

        <Link to="/register" className="btn btn-primary hero-btn">
          Get Started
          <ArrowRight size={18} />
        </Link>

      </section>

    </div>
  );
};

export default LandingPage;
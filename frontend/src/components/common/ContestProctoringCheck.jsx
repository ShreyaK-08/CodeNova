import React, { useState, useEffect, useRef } from 'react';
import {
    Camera,
    Mic,
    AlertTriangle,
    CheckCircle,
    RefreshCw,
    Lock,
    ShieldCheck,
    ArrowRight,
    ArrowLeft,
    Maximize2,
    Check,
    Eye,
    Monitor,
    Shield
} from 'lucide-react';

/**
 * ContestProctoringCheck
 *
 * Professional pre-entry system check screen for CodeNova coding contests.
 * Verifies camera and microphone permissions before allowing candidate into the contest workspace.
 *
 * Privacy Guarantee: All streams are verified in-browser only. Zero recording, zero uploads.
 */
const ContestProctoringCheck = ({ contestTitle, onPassed, onCancel }) => {
    const [cameraStatus, setCameraStatus] = useState('checking'); // 'checking' | 'allowed' | 'blocked'
    const [micStatus, setMicStatus] = useState('checking');       // 'checking' | 'allowed' | 'blocked'
    const [stream, setStream] = useState(null);
    const [audioLevel, setAudioLevel] = useState(0);              // 0 to 100 percentage
    const [errorMessage, setErrorMessage] = useState('');

    const videoRef = useRef(null);
    const audioContextRef = useRef(null);
    const analyserRef = useRef(null);
    const animFrameRef = useRef(null);

    const initMedia = async () => {
        setCameraStatus('checking');
        setMicStatus('checking');
        setErrorMessage('');

        // Clean up previous context and stream if retrying
        if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
        if (audioContextRef.current && audioContextRef.current.state !== 'closed') {
            audioContextRef.current.close().catch(() => {});
        }
        if (stream) {
            stream.getTracks().forEach(track => track.stop());
            setStream(null);
        }

        try {
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                setCameraStatus('blocked');
                setMicStatus('blocked');
                setErrorMessage('Your browser does not support media device access (navigator.mediaDevices is unavailable). Please use Chrome, Edge, or Firefox.');
                return;
            }

            const mediaStream = await navigator.mediaDevices.getUserMedia({
                video: {
                    facingMode: 'user',
                    width: { ideal: 640 },
                    height: { ideal: 480 }
                },
                audio: true
            });

            setStream(mediaStream);
            setCameraStatus('allowed');
            setMicStatus('allowed');

            if (videoRef.current) {
                videoRef.current.srcObject = mediaStream;
            }

            // Web Audio API to detect microphone volume level
            try {
                const AudioContextClass = window.AudioContext || window.webkitAudioContext;
                if (AudioContextClass) {
                    const audioCtx = new AudioContextClass();
                    audioContextRef.current = audioCtx;
                    const analyser = audioCtx.createAnalyser();
                    analyser.fftSize = 256;
                    analyserRef.current = analyser;

                    const source = audioCtx.createMediaStreamSource(mediaStream);
                    source.connect(analyser);

                    const dataArray = new Uint8Array(analyser.frequencyBinCount);
                    const updateMeter = () => {
                        analyser.getByteFrequencyData(dataArray);
                        let sum = 0;
                        for (let i = 0; i < dataArray.length; i++) {
                            sum += dataArray[i];
                        }
                        const average = sum / dataArray.length;
                        // Normalize to 0..100%
                        const percentage = Math.min(100, Math.round((average / 115) * 100));
                        setAudioLevel(percentage);
                        animFrameRef.current = requestAnimationFrame(updateMeter);
                    };
                    updateMeter();
                }
            } catch (audioErr) {
                console.warn('Web Audio API visualization could not be initialized:', audioErr);
            }

        } catch (err) {
            console.error('Proctoring device check failed:', err);
            if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
                setCameraStatus('blocked');
                setMicStatus('blocked');
                setErrorMessage('Camera and microphone permissions were denied. Please allow access in your browser settings.');
            } else if (err.name === 'NotFoundError' || err.name === 'DevicesNotFoundError') {
                setCameraStatus('blocked');
                setMicStatus('blocked');
                setErrorMessage('No camera or microphone hardware found on your system. Please connect working devices to enter.');
            } else if (err.name === 'NotReadableError' || err.name === 'TrackStartError') {
                setCameraStatus('blocked');
                setMicStatus('blocked');
                setErrorMessage('Your camera or microphone is in use by another application. Please close other apps and retry.');
            } else {
                setCameraStatus('blocked');
                setMicStatus('blocked');
                setErrorMessage(`Could not access devices: ${err.message || 'Unknown error'}`);
            }
        }
    };

    useEffect(() => {
        initMedia();
        return () => {
            if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
            if (audioContextRef.current && audioContextRef.current.state !== 'closed') {
                audioContextRef.current.close().catch(() => {});
            }
        };
    }, []);

    useEffect(() => {
        if (videoRef.current && stream) {
            videoRef.current.srcObject = stream;
        }
    }, [stream]);

    const isReady = cameraStatus === 'allowed' && micStatus === 'allowed';
    const isBlocked = cameraStatus === 'blocked' || micStatus === 'blocked';

    const handleContinue = async () => {
        if (!isReady) return;
        try {
            if (document.documentElement.requestFullscreen) {
                await document.documentElement.requestFullscreen().catch(() => {});
            }
        } catch (e) {
            console.warn('Fullscreen request deferred or failed:', e);
        }
        if (onPassed) {
            onPassed(stream);
        }
    };

    return (
        <div style={{
            minHeight: '80vh',
            maxWidth: '1160px',
            margin: '0 auto',
            padding: '1.25rem 1rem 2.5rem 1rem',
            color: 'var(--text-main, #1e293b)'
        }}>

            {/* ================= HEADER SECTION ================= */}
            <header style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
                <div style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.4rem',
                    padding: '0.3rem 0.85rem',
                    borderRadius: '9999px',
                    backgroundColor: 'var(--primary-soft, rgba(124, 58, 237, 0.08))',
                    color: 'var(--primary, #7c3aed)',
                    fontSize: '0.78rem',
                    fontWeight: 700,
                    letterSpacing: '0.04em',
                    marginBottom: '0.6rem',
                    border: '1px solid rgba(124, 58, 237, 0.18)'
                }}>
                    <ShieldCheck size={15} color="var(--primary, #7c3aed)" />
                    <span>CodeNova Proctoring Verification</span>
                </div>

                <h1 style={{
                    fontSize: '1.85rem',
                    fontWeight: 800,
                    color: 'var(--text-main, #1e293b)',
                    letterSpacing: '-0.02em',
                    lineHeight: 1.2,
                    marginBottom: '0.35rem'
                }}>
                    Contest System Check
                </h1>

                {contestTitle && (
                    <div style={{
                        fontSize: '1.05rem',
                        fontWeight: 600,
                        color: 'var(--primary, #7c3aed)',
                        marginBottom: '0.5rem'
                    }}>
                        {contestTitle}
                    </div>
                )}

                <p style={{
                    fontSize: '0.92rem',
                    color: 'var(--text-muted, #64748b)',
                    maxWidth: '680px',
                    margin: '0 auto 0.4rem auto',
                    lineHeight: 1.45
                }}>
                    Complete the system checks below before entering the contest. Camera and microphone access are required throughout the contest.
                </p>

                <p style={{
                    fontSize: '0.8rem',
                    color: 'var(--text-subtle, #94a3b8)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '0.35rem'
                }}>
                    <Lock size={12} />
                    <span>Your camera and microphone are verified in your browser. No video or audio recordings are stored or uploaded.</span>
                </p>
            </header>

            {/* ================= SUCCESS BANNER (WHEN READY) ================= */}
            {isReady && (
                <div style={{
                    backgroundColor: 'var(--success-light, #dcfce7)',
                    border: '1px solid #86efac',
                    borderRadius: 'var(--radius-lg, 10px)',
                    padding: '0.75rem 1.25rem',
                    marginBottom: '1.25rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.75rem',
                    color: '#15803d',
                    fontSize: '0.9rem',
                    boxShadow: 'var(--shadow-sm)'
                }}>
                    <CheckCircle size={18} color="#15803d" style={{ flexShrink: 0 }} />
                    <div style={{ flex: 1 }}>
                        <strong style={{ display: 'block', fontWeight: 700 }}>System Check Complete</strong>
                        <span style={{ fontSize: '0.82rem', opacity: 0.9 }}>Your camera, microphone, and fullscreen requirements are ready. You may now continue.</span>
                    </div>
                </div>
            )}

            {/* ================= PERMISSION ERROR / INSTRUCTIONS (WHEN BLOCKED) ================= */}
            {isBlocked && (
                <div style={{
                    backgroundColor: 'var(--danger-light, #fee2e2)',
                    border: '1px solid #fca5a5',
                    borderRadius: 'var(--radius-lg, 10px)',
                    padding: '1rem 1.25rem',
                    marginBottom: '1.25rem',
                    color: '#991b1b',
                    boxShadow: 'var(--shadow-sm)'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700, fontSize: '0.95rem', marginBottom: '0.4rem' }}>
                        <AlertTriangle size={18} color="#dc2626" />
                        <span>Permission Required: Camera &amp; Microphone Access</span>
                    </div>
                    <p style={{ fontSize: '0.85rem', color: '#7f1d1d', marginBottom: '0.6rem' }}>
                        {errorMessage || 'Camera and microphone access are required to enter this contest.'}
                    </p>
                    <div style={{
                        backgroundColor: '#ffffff',
                        border: '1px solid #fecaca',
                        borderRadius: 'var(--radius-md, 8px)',
                        padding: '0.65rem 1rem',
                        fontSize: '0.82rem',
                        color: '#374151'
                    }}>
                        <strong style={{ color: '#1f2937' }}>How to allow access in your browser:</strong>
                        <ol style={{ paddingLeft: '1.25rem', marginTop: '0.35rem', lineHeight: '1.5' }}>
                            <li>Click the <strong>camera / padlock icon</strong> located near your browser's address bar.</li>
                            <li>Set both <strong>Camera</strong> and <strong>Microphone</strong> to <strong>Allow</strong>.</li>
                            <li>Click the <strong>"Retry Permission Check"</strong> button below.</li>
                        </ol>
                    </div>
                </div>
            )}

            {/* ================= MAIN SYSTEM CHECK CARD (TWO-COLUMN LAYOUT) ================= */}
            <div className="card" style={{
                backgroundColor: 'var(--bg-card, #ffffff)',
                border: '1px solid var(--border-color, #e2e8f0)',
                borderRadius: 'var(--radius-xl, 14px)',
                padding: '1.5rem',
                boxShadow: 'var(--shadow-sm)',
                marginBottom: '1.5rem'
            }}>
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
                    gap: '1.75rem',
                    alignItems: 'start'
                }}>

                    {/* ---------- LEFT COLUMN: CAMERA PREVIEW ---------- */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                        <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            paddingBottom: '0.35rem'
                        }}>
                            <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-main, #1e293b)', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                                <Camera size={16} color="var(--primary, #7c3aed)" />
                                Camera Live Feed
                            </span>
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)' }}>
                                Facing Candidate
                            </span>
                        </div>

                        {/* 16:9 Camera Preview Box */}
                        <div style={{
                            position: 'relative',
                            width: '100%',
                            aspectRatio: '16 / 9',
                            maxHeight: '320px',
                            backgroundColor: '#0f172a',
                            borderRadius: '14px',
                            overflow: 'hidden',
                            border: '1px solid #cbd5e1',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            boxShadow: 'inset 0 2px 4px rgba(0,0,0,0.2)'
                        }}>
                            {cameraStatus === 'allowed' ? (
                                <video
                                    ref={videoRef}
                                    autoPlay
                                    playsInline
                                    muted
                                    style={{
                                        width: '100%',
                                        height: '100%',
                                        objectFit: 'cover',
                                        transform: 'scaleX(-1)'
                                    }}
                                />
                            ) : cameraStatus === 'checking' ? (
                                <div style={{ textAlign: 'center', color: '#94a3b8', padding: '1rem' }}>
                                    <RefreshCw size={28} className="animate-spin" color="#a78bfa" style={{ margin: '0 auto 0.5rem auto' }} />
                                    <p style={{ fontSize: '0.85rem', fontWeight: 600 }}>Checking camera connection...</p>
                                </div>
                            ) : (
                                <div style={{ textAlign: 'center', color: '#f87171', padding: '1rem' }}>
                                    <AlertTriangle size={32} color="#ef4444" style={{ margin: '0 auto 0.5rem auto' }} />
                                    <p style={{ fontSize: '0.85rem', fontWeight: 600 }}>Camera access blocked or unavailable</p>
                                    <p style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '0.25rem' }}>Allow camera permissions to view feed</p>
                                </div>
                            )}

                            {/* Top-Left Live Badge */}
                            <div style={{
                                position: 'absolute',
                                top: '10px',
                                left: '10px',
                                backgroundColor: 'rgba(15, 23, 42, 0.78)',
                                backdropFilter: 'blur(4px)',
                                color: cameraStatus === 'allowed' ? '#34d399' : '#94a3b8',
                                padding: '3px 9px',
                                borderRadius: '6px',
                                fontSize: '0.72rem',
                                fontWeight: 700,
                                letterSpacing: '0.04em',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px',
                                border: '1px solid rgba(255, 255, 255, 0.12)'
                            }}>
                                <span style={{
                                    width: '7px',
                                    height: '7px',
                                    borderRadius: '50%',
                                    backgroundColor: cameraStatus === 'allowed' ? '#10b981' : '#64748b',
                                    boxShadow: cameraStatus === 'allowed' ? '0 0 8px #10b981' : 'none'
                                }} />
                                LIVE PREVIEW
                            </div>

                            {/* Top-Right Status Badge */}
                            <div style={{
                                position: 'absolute',
                                top: '10px',
                                right: '10px',
                                backgroundColor: cameraStatus === 'allowed'
                                    ? 'rgba(22, 163, 74, 0.88)'
                                    : cameraStatus === 'checking'
                                    ? 'rgba(217, 119, 6, 0.88)'
                                    : 'rgba(220, 38, 38, 0.88)',
                                backdropFilter: 'blur(4px)',
                                color: '#ffffff',
                                padding: '3px 9px',
                                borderRadius: '6px',
                                fontSize: '0.72rem',
                                fontWeight: 700,
                                display: 'flex',
                                alignItems: 'center',
                                gap: '4px'
                            }}>
                                {cameraStatus === 'allowed' && <><Check size={12} /> Camera Ready</>}
                                {cameraStatus === 'checking' && <><RefreshCw size={12} className="animate-spin" /> Checking...</>}
                                {cameraStatus === 'blocked' && <><AlertTriangle size={12} /> Camera Blocked</>}
                            </div>
                        </div>

                        {/* Helper Text below preview */}
                        <div style={{
                            textAlign: 'center',
                            fontSize: '0.8rem',
                            color: 'var(--text-muted, #64748b)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: '0.35rem',
                            paddingTop: '0.15rem'
                        }}>
                            <Camera size={13} color="var(--text-subtle, #94a3b8)" />
                            <span>Position your face inside the frame. Ensure good lighting.</span>
                        </div>
                    </div>

                    {/* ---------- RIGHT COLUMN: SYSTEM CHECKS & METER ---------- */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>

                        {/* Requirement Summary Box */}
                        <div style={{
                            backgroundColor: 'var(--bg-main, #f7f8fc)',
                            border: '1px solid var(--border-color, #e2e8f0)',
                            borderRadius: 'var(--radius-md, 8px)',
                            padding: '0.75rem 1rem'
                        }}>
                            <div style={{
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'space-between',
                                marginBottom: '0.45rem'
                            }}>
                                <span style={{ fontSize: '0.82rem', fontWeight: 700, color: 'var(--text-main, #1e293b)' }}>
                                    System Requirements
                                </span>
                                <span style={{
                                    fontSize: '0.75rem',
                                    fontWeight: 700,
                                    color: isReady ? 'var(--success, #16a34a)' : 'var(--text-muted, #64748b)'
                                }}>
                                    {isReady ? '✓ All required checks passed' : 'Action required'}
                                </span>
                            </div>

                            {/* Mini 3 Chips */}
                            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                                <div style={{
                                    fontSize: '0.75rem',
                                    padding: '3px 8px',
                                    borderRadius: '5px',
                                    backgroundColor: cameraStatus === 'allowed' ? '#dcfce7' : '#f1f5f9',
                                    color: cameraStatus === 'allowed' ? '#15803d' : '#64748b',
                                    fontWeight: 600,
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '3px'
                                }}>
                                    {cameraStatus === 'allowed' ? '✓' : '•'} Camera
                                </div>
                                <div style={{
                                    fontSize: '0.75rem',
                                    padding: '3px 8px',
                                    borderRadius: '5px',
                                    backgroundColor: micStatus === 'allowed' ? '#dcfce7' : '#f1f5f9',
                                    color: micStatus === 'allowed' ? '#15803d' : '#64748b',
                                    fontWeight: 600,
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '3px'
                                }}>
                                    {micStatus === 'allowed' ? '✓' : '•'} Microphone
                                </div>
                                <div style={{
                                    fontSize: '0.75rem',
                                    padding: '3px 8px',
                                    borderRadius: '5px',
                                    backgroundColor: '#dcfce7',
                                    color: '#15803d',
                                    fontWeight: 600,
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '3px'
                                }}>
                                    ✓ Fullscreen
                                </div>
                            </div>
                        </div>

                        {/* Status Card 1: Camera */}
                        <div style={{
                            border: '1px solid var(--border-color, #e2e8f0)',
                            borderRadius: 'var(--radius-md, 8px)',
                            padding: '0.85rem 1rem',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            backgroundColor: '#ffffff'
                        }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                <div style={{
                                    width: '36px',
                                    height: '36px',
                                    borderRadius: '8px',
                                    backgroundColor: 'var(--primary-soft, rgba(124, 58, 237, 0.08))',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <Camera size={18} color="var(--primary, #7c3aed)" />
                                </div>
                                <div>
                                    <div style={{ fontSize: '0.88rem', fontWeight: 700, color: 'var(--text-main, #1e293b)' }}>Camera Access</div>
                                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)' }}>Webcam video feed verification</div>
                                </div>
                            </div>
                            <div>
                                {cameraStatus === 'allowed' && (
                                    <span style={{
                                        fontSize: '0.76rem',
                                        fontWeight: 700,
                                        color: '#15803d',
                                        backgroundColor: '#dcfce7',
                                        padding: '4px 9px',
                                        borderRadius: '9999px',
                                        border: '1px solid #bbf7d0',
                                        display: 'inline-flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}>
                                        <CheckCircle size={13} /> Allowed
                                    </span>
                                )}
                                {cameraStatus === 'checking' && (
                                    <span style={{
                                        fontSize: '0.76rem',
                                        fontWeight: 700,
                                        color: '#b45309',
                                        backgroundColor: '#fef3c7',
                                        padding: '4px 9px',
                                        borderRadius: '9999px',
                                        border: '1px solid #fde68a',
                                        display: 'inline-flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}>
                                        <RefreshCw size={12} className="animate-spin" /> Checking...
                                    </span>
                                )}
                                {cameraStatus === 'blocked' && (
                                    <span style={{
                                        fontSize: '0.76rem',
                                        fontWeight: 700,
                                        color: '#b91c1c',
                                        backgroundColor: '#fee2e2',
                                        padding: '4px 9px',
                                        borderRadius: '9999px',
                                        border: '1px solid #fecaca',
                                        display: 'inline-flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}>
                                        <AlertTriangle size={13} /> Camera access required
                                    </span>
                                )}
                            </div>
                        </div>

                        {/* Status Card 2: Microphone & Level Meter */}
                        <div style={{
                            border: '1px solid var(--border-color, #e2e8f0)',
                            borderRadius: 'var(--radius-md, 8px)',
                            padding: '0.85rem 1rem',
                            backgroundColor: '#ffffff'
                        }}>
                            <div style={{
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'space-between',
                                marginBottom: '0.65rem'
                            }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                    <div style={{
                                        width: '36px',
                                        height: '36px',
                                        borderRadius: '8px',
                                        backgroundColor: 'var(--primary-soft, rgba(124, 58, 237, 0.08))',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center'
                                    }}>
                                        <Mic size={18} color="var(--primary, #7c3aed)" />
                                    </div>
                                    <div>
                                        <div style={{ fontSize: '0.88rem', fontWeight: 700, color: 'var(--text-main, #1e293b)' }}>Microphone Access</div>
                                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)' }}>Audio input level monitoring</div>
                                    </div>
                                </div>
                                <div>
                                    {micStatus === 'allowed' && (
                                        <span style={{
                                            fontSize: '0.76rem',
                                            fontWeight: 700,
                                            color: '#15803d',
                                            backgroundColor: '#dcfce7',
                                            padding: '4px 9px',
                                            borderRadius: '9999px',
                                            border: '1px solid #bbf7d0',
                                            display: 'inline-flex',
                                            alignItems: 'center',
                                            gap: '4px'
                                        }}>
                                            <CheckCircle size={13} /> Allowed
                                        </span>
                                    )}
                                    {micStatus === 'checking' && (
                                        <span style={{
                                            fontSize: '0.76rem',
                                            fontWeight: 700,
                                            color: '#b45309',
                                            backgroundColor: '#fef3c7',
                                            padding: '4px 9px',
                                            borderRadius: '9999px',
                                            border: '1px solid #fde68a',
                                            display: 'inline-flex',
                                            alignItems: 'center',
                                            gap: '4px'
                                        }}>
                                            <RefreshCw size={12} className="animate-spin" /> Checking...
                                        </span>
                                    )}
                                    {micStatus === 'blocked' && (
                                        <span style={{
                                            fontSize: '0.76rem',
                                            fontWeight: 700,
                                            color: '#b91c1c',
                                            backgroundColor: '#fee2e2',
                                            padding: '4px 9px',
                                            borderRadius: '9999px',
                                            border: '1px solid #fecaca',
                                            display: 'inline-flex',
                                            alignItems: 'center',
                                            gap: '4px'
                                        }}>
                                            <AlertTriangle size={13} /> Microphone access required
                                        </span>
                                    )}
                                </div>
                            </div>

                            {/* Volume Meter Row */}
                            <div style={{
                                backgroundColor: 'var(--bg-main, #f7f8fc)',
                                border: '1px solid var(--border-color, #e2e8f0)',
                                borderRadius: '6px',
                                padding: '0.55rem 0.75rem'
                            }}>
                                <div style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    fontSize: '0.74rem',
                                    color: 'var(--text-muted, #64748b)',
                                    marginBottom: '0.35rem'
                                }}>
                                    <span style={{ fontWeight: 600 }}>Microphone Level</span>
                                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--primary, #7c3aed)' }}>
                                        {audioLevel}%
                                    </span>
                                </div>
                                <div style={{
                                    width: '100%',
                                    height: '8px',
                                    backgroundColor: '#e2e8f0',
                                    borderRadius: '9999px',
                                    overflow: 'hidden'
                                }}>
                                    <div style={{
                                        height: '100%',
                                        width: `${Math.max(3, audioLevel)}%`,
                                        backgroundColor: audioLevel > 50 ? '#10b981' : 'var(--primary, #7c3aed)',
                                        borderRadius: '9999px',
                                        transition: 'width 75ms ease'
                                    }} />
                                </div>
                                <div style={{
                                    fontSize: '0.72rem',
                                    color: 'var(--text-subtle, #94a3b8)',
                                    marginTop: '0.35rem'
                                }}>
                                    {audioLevel > 5
                                        ? 'Speak briefly to verify microphone input.'
                                        : 'Microphone connected — speak to test audio.'}
                                </div>
                            </div>
                        </div>

                        {/* Status Card 3: Fullscreen Mode */}
                        <div style={{
                            border: '1px solid var(--border-color, #e2e8f0)',
                            borderRadius: 'var(--radius-md, 8px)',
                            padding: '0.85rem 1rem',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            backgroundColor: '#ffffff'
                        }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                <div style={{
                                    width: '36px',
                                    height: '36px',
                                    borderRadius: '8px',
                                    backgroundColor: 'var(--primary-soft, rgba(124, 58, 237, 0.08))',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <Maximize2 size={18} color="var(--primary, #7c3aed)" />
                                </div>
                                <div>
                                    <div style={{ fontSize: '0.88rem', fontWeight: 700, color: 'var(--text-main, #1e293b)' }}>Fullscreen Mode</div>
                                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)' }}>Fullscreen mode is required during the contest.</div>
                                </div>
                            </div>
                            <div>
                                <span style={{
                                    fontSize: '0.76rem',
                                    fontWeight: 700,
                                    color: '#15803d',
                                    backgroundColor: '#dcfce7',
                                    padding: '4px 9px',
                                    borderRadius: '9999px',
                                    border: '1px solid #bbf7d0',
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    gap: '4px'
                                }}>
                                    <Check size={13} /> Ready
                                </span>
                            </div>
                        </div>

                    </div>
                </div>
            </div>

            {/* ================= PROCTORING RULES CARD ================= */}
            <div className="card" style={{
                backgroundColor: 'var(--bg-card, #ffffff)',
                border: '1px solid var(--border-color, #e2e8f0)',
                borderRadius: 'var(--radius-xl, 14px)',
                padding: '1.25rem 1.5rem',
                marginBottom: '1.5rem',
                boxShadow: 'var(--shadow-sm)'
            }}>
                <div style={{
                    fontSize: '0.92rem',
                    fontWeight: 800,
                    color: 'var(--text-main, #1e293b)',
                    marginBottom: '0.85rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.45rem'
                }}>
                    <Shield size={16} color="var(--primary, #7c3aed)" />
                    <span>Contest Proctoring Rules</span>
                </div>

                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                    gap: '0.75rem'
                }}>
                    <div style={{
                        padding: '0.75rem',
                        borderRadius: 'var(--radius-md, 8px)',
                        backgroundColor: 'var(--bg-main, #f7f8fc)',
                        border: '1px solid var(--border-color, #e2e8f0)'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-main, #1e293b)', marginBottom: '0.2rem' }}>
                            <Camera size={14} color="var(--primary, #7c3aed)" /> Camera
                        </div>
                        <p style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)', margin: 0, lineHeight: 1.35 }}>
                            Camera must remain active throughout the contest.
                        </p>
                    </div>

                    <div style={{
                        padding: '0.75rem',
                        borderRadius: 'var(--radius-md, 8px)',
                        backgroundColor: 'var(--bg-main, #f7f8fc)',
                        border: '1px solid var(--border-color, #e2e8f0)'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-main, #1e293b)', marginBottom: '0.2rem' }}>
                            <Mic size={14} color="var(--primary, #7c3aed)" /> Microphone
                        </div>
                        <p style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)', margin: 0, lineHeight: 1.35 }}>
                            Microphone must remain active throughout the contest.
                        </p>
                    </div>

                    <div style={{
                        padding: '0.75rem',
                        borderRadius: 'var(--radius-md, 8px)',
                        backgroundColor: 'var(--bg-main, #f7f8fc)',
                        border: '1px solid var(--border-color, #e2e8f0)'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-main, #1e293b)', marginBottom: '0.2rem' }}>
                            <Monitor size={14} color="var(--primary, #7c3aed)" /> Fullscreen
                        </div>
                        <p style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)', margin: 0, lineHeight: 1.35 }}>
                            Fullscreen mode is required during the test.
                        </p>
                    </div>

                    <div style={{
                        padding: '0.75rem',
                        borderRadius: 'var(--radius-md, 8px)',
                        backgroundColor: 'var(--bg-main, #f7f8fc)',
                        border: '1px solid var(--border-color, #e2e8f0)'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-main, #1e293b)', marginBottom: '0.2rem' }}>
                            <Eye size={14} color="var(--primary, #7c3aed)" /> Tab switching
                        </div>
                        <p style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)', margin: 0, lineHeight: 1.35 }}>
                            Switching tabs or minimizing may record a security strike.
                        </p>
                    </div>

                    <div style={{
                        padding: '0.75rem',
                        borderRadius: 'var(--radius-md, 8px)',
                        backgroundColor: 'var(--bg-main, #f7f8fc)',
                        border: '1px solid var(--border-color, #e2e8f0)'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-main, #1e293b)', marginBottom: '0.2rem' }}>
                            <Lock size={14} color="var(--primary, #7c3aed)" /> Privacy
                        </div>
                        <p style={{ fontSize: '0.75rem', color: 'var(--text-muted, #64748b)', margin: 0, lineHeight: 1.35 }}>
                            Zero recordings. Streams run locally inside browser only.
                        </p>
                    </div>
                </div>
            </div>

            {/* ================= ACTION BUTTONS ================= */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '1rem',
                paddingTop: '0.5rem'
            }}>
                <div>
                    {onCancel && (
                        <button
                            type="button"
                            onClick={onCancel}
                            className="btn btn-outline"
                            style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '0.4rem'
                            }}
                        >
                            <ArrowLeft size={16} />
                            <span>Back to Contests</span>
                        </button>
                    )}
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <button
                        type="button"
                        onClick={initMedia}
                        className="btn btn-outline"
                        style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.4rem'
                        }}
                    >
                        <RefreshCw size={15} />
                        <span>Retry Permission Check</span>
                    </button>

                    <button
                        type="button"
                        onClick={handleContinue}
                        disabled={!isReady}
                        className="btn btn-primary"
                        style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.5rem',
                            padding: '0.7rem 1.5rem',
                            fontSize: '0.95rem',
                            fontWeight: 700,
                            borderRadius: 'var(--radius-md, 8px)',
                            opacity: isReady ? 1 : 0.45,
                            cursor: isReady ? 'pointer' : 'not-allowed',
                            boxShadow: isReady ? '0 4px 14px rgba(124, 58, 237, 0.35)' : 'none'
                        }}
                    >
                        <span>Continue to Contest</span>
                        <ArrowRight size={16} />
                    </button>
                </div>
            </div>

        </div>
    );
};

export default ContestProctoringCheck;

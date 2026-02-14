import React, {useState} from 'react';
import {useNavigate, Link} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import './Auth.css';
import axios from "axios";

const Login: React.FC = () => {
    const navigate = useNavigate();
    const {login} = useAuth();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });
    const [error, setError] = useState<string>('');
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
        setError(''); // Clear error when user types
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            await login(formData);
            navigate('/'); // Redirect to home after successful login
        } catch (err: unknown) {
            console.error('Login error:', err);

            // Extract error message from different possible error structures
            let errorMessage = 'An unexpected error occurred. Please try again later.';

            if (axios.isAxiosError(err)) {
                const status = err.response?.status;

                if (status === 401) {
                    errorMessage = 'Invalid email or password';
                } else if (status === 404) {
                    errorMessage = 'User not found. Please check your email.';
                } else if (err.response?.data) {
                    const data = err.response.data;
                    errorMessage = data;
                }

                setError(errorMessage);
            }
        } finally
            {
                setIsLoading(false);
            }
        }
        ;

        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <h1>Welcome Back</h1>
                        <p>Sign in to your account</p>
                    </div>

                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="auth-form">
                        <div className="form-group">
                            <label htmlFor="email">Email</label>
                            <input
                                type="email"
                                id="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                required
                                placeholder="your.email@example.com"
                                disabled={isLoading}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="password">Password</label>
                            <input
                                type="password"
                                id="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                required
                                placeholder="Enter your password"
                                disabled={isLoading}
                            />
                        </div>

                        <button
                            type="submit"
                            className="btn-primary"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Signing in...' : 'Sign In'}
                        </button>
                    </form>

                    <div className="auth-footer">
                        <p>
                            Don't have an account?{' '}
                            <Link to="/register">Sign up</Link>
                        </p>
                    </div>
                </div>
            </div>
        );
    };

    export default Login;
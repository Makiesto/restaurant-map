import React, {useState, useRef} from 'react';
import axios, {type AxiosError} from 'axios';
import './ImageUploadField.css';

interface ImageUploadFieldProps {
    label?: string;
    value: string;
    onChange: (url: string) => void;
    folder?: 'restaurants' | 'dishes';
    disabled?: boolean;
}

const ImageUploadField: React.FC<ImageUploadFieldProps> = ({
                                                               label = 'Image',
                                                               value,
                                                               onChange,
                                                               folder = 'restaurants',
                                                               disabled = false,
                                                           }) => {
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState<string>('');
    const [dragActive, setDragActive] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

    const validateFile = (file: File): string | null => {
        // Check file type
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(file.type)) {
            return 'Please upload an image file (JPEG, PNG, GIF, or WebP)';
        }

        // Check file size (10MB)
        const maxSize = 10 * 1024 * 1024;
        if (file.size > maxSize) {
            return 'File size must be less than 10MB';
        }

        return null;
    };

    const handleUpload = async (file: File) => {
        const validationError = validateFile(file);
        if (validationError) {
            setError(validationError);
            return;
        }

        setUploading(true);
        setError('');

        try {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('folder', folder);

            const token = localStorage.getItem('token');
            const response = await axios.post(`${API_URL}/upload/image`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    ...(token ? {Authorization: `Bearer ${token}`} : {}),
                },
            });

            onChange(response.data.url);
        } catch (err: unknown) {
            console.error('Upload error:', err);

            const axiosError = err as AxiosError<{ message: string }>;
            const errorMessage = axiosError.response?.data?.message || 'Failed to upload image';

            setError(errorMessage);
        } finally {
            setUploading(false);
        }
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            handleUpload(file);
        }
    };

    const handleDrag = (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === 'dragenter' || e.type === 'dragover') {
            setDragActive(true);
        } else if (e.type === 'dragleave') {
            setDragActive(false);
        }
    };

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);

        if (disabled) return;

        const file = e.dataTransfer.files?.[0];
        if (file) {
            handleUpload(file);
        }
    };

    const handleRemove = async () => {
        if (!value) return;

        if (!confirm('Remove this image?')) return;

        try {
            const token = localStorage.getItem('token');
            await axios.delete(`${API_URL}/upload/image`, {
                params: {url: value},
                headers: token ? {Authorization: `Bearer ${token}`} : {},
            });

            onChange('');
        } catch (err) {
            console.error('Error removing image:', err);
            // Still remove from form even if delete fails
            onChange('');
        }
    };

    const handleClick = () => {
        if (!disabled) {
            fileInputRef.current?.click();
        }
    };

    return (
        <div className="image-upload-field">
            {label && <label className="upload-label">{label}</label>}

            {error && <div className="upload-error">{error}</div>}

            <div
                className={`upload-area ${dragActive ? 'drag-active' : ''} ${
                    disabled ? 'disabled' : ''
                }`}
                onDragEnter={handleDrag}
                onDragLeave={handleDrag}
                onDragOver={handleDrag}
                onDrop={handleDrop}
                onClick={handleClick}
            >
                <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleFileChange}
                    disabled={disabled || uploading}
                    style={{display: 'none'}}
                />

                {uploading ? (
                    <div className="upload-status">
                        <div className="spinner-small"></div>
                        <p>Uploading...</p>
                    </div>
                ) : value ? (
                    <div className="image-preview">
                        <img src={value} alt="Preview"/>
                        <div className="image-overlay">
                            <button
                                type="button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleRemove();
                                }}
                                className="btn-remove-image"
                                disabled={disabled}
                            >
                                Remove
                            </button>
                            <button
                                type="button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    fileInputRef.current?.click();
                                }}
                                className="btn-change-image"
                                disabled={disabled}
                            >
                                Change
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="upload-placeholder">
                        <div className="upload-icon">📷</div>
                        <p className="upload-text">Click to upload or drag and drop</p>
                        <p className="upload-hint">PNG, JPG, GIF or WebP (max. 10MB)</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ImageUploadField;
import React, {useState, useEffect} from 'react';
import {useParams} from 'react-router-dom';
import axios from 'axios';

interface Restaurant {
    id: number;
    name: string;
    description: string;
    cuisine: string;
    address: string;
    phone: string;
    opening_hours: string;
}

const RestaurantDetails: React.FC = () => {
    const {restaurantId} = useParams<{ restaurantId: string }>();
    const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchRestaurantData = async (id: number) => {
            try {
                setLoading(true);
                setError(null);
                const response = await axios.get(`http://localhost:8080/api/restaurants/${id}`);
                setRestaurant(response.data);
            } catch (err: unknown) {
                let errorMessage = 'Failed to load restaurant details';

                if (axios.isAxiosError(err)) {
                    errorMessage = err.response?.data?.detail || err.response?.data?.message || errorMessage;
                } else if (err instanceof Error) {
                    errorMessage = err.message;
                }

                setError(errorMessage);
            } finally {
                setLoading(false);
            }
        };

        if (restaurantId) {
            fetchRestaurantData(parseInt(restaurantId));
        }
    }, [restaurantId]);

    if (loading) {
        return (
            <div style={styles.container}>
                <div style={styles.loadingSpinner}></div>
            </div>
        );
    }

    if (error) {
        return (
            <div style={styles.container}>
                <div style={styles.errorCard}>
                    <h2 style={styles.errorTitle}>Unable to Load Restaurant</h2>
                    <p style={styles.errorMessage}>{error}</p>
                </div>
            </div>
        );
    }

    if (!restaurant) {
        return (
            <div style={styles.container}>
                <div style={styles.errorCard}>
                    <p style={styles.errorMessage}>Restaurant not found</p>
                </div>
            </div>
        );
    }

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <header style={styles.header}>
                    <h1 style={styles.restaurantName}>{restaurant.name}</h1>
                    <span style={styles.cuisineBadge}>{restaurant.cuisine}</span>
                </header>

                <div style={styles.section}>
                    <p style={styles.description}>{restaurant.description}</p>
                </div>

                <div style={styles.divider}></div>

                <div style={styles.infoGrid}>
                    <div style={styles.infoItem}>
                        <svg style={styles.icon} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
                        </svg>
                        <div>
                            <div style={styles.infoLabel}>Address</div>
                            <div style={styles.infoValue}>{restaurant.address}</div>
                        </div>
                    </div>

                    <div style={styles.infoItem}>
                        <svg style={styles.icon} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
                        </svg>
                        <div>
                            <div style={styles.infoLabel}>Phone</div>
                            <div style={styles.infoValue}>{restaurant.phone}</div>
                        </div>
                    </div>

                    <div style={styles.infoItem}>
                        <svg style={styles.icon} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        <div>
                            <div style={styles.infoLabel}>Hours</div>
                            <div style={styles.infoValue}>{restaurant.opening_hours}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

const styles: { [key: string]: React.CSSProperties } = {
    container: {
        minHeight: '100vh',
        backgroundColor: '#fafafa',
        padding: '40px 20px',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'flex-start',
    },
    card: {
        backgroundColor: '#ffffff',
        borderRadius: '8px',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        maxWidth: '700px',
        width: '100%',
        padding: '40px',
    },
    header: {
        marginBottom: '24px',
    },
    restaurantName: {
        fontSize: '32px',
        fontWeight: '600',
        color: '#1a1a1a',
        margin: '0 0 12px 0',
        letterSpacing: '-0.5px',
    },
    cuisineBadge: {
        display: 'inline-block',
        backgroundColor: '#f5f5f5',
        color: '#666',
        padding: '6px 14px',
        borderRadius: '20px',
        fontSize: '14px',
        fontWeight: '500',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
    },
    section: {
        marginBottom: '28px',
    },
    description: {
        fontSize: '16px',
        lineHeight: '1.6',
        color: '#4a4a4a',
        margin: 0,
    },
    divider: {
        height: '1px',
        backgroundColor: '#e5e5e5',
        margin: '32px 0',
    },
    infoGrid: {
        display: 'flex',
        flexDirection: 'column',
        gap: '20px',
    },
    infoItem: {
        display: 'flex',
        alignItems: 'flex-start',
        gap: '16px',
    },
    icon: {
        width: '24px',
        height: '24px',
        color: '#999',
        flexShrink: 0,
        marginTop: '2px',
    },
    infoLabel: {
        fontSize: '12px',
        textTransform: 'uppercase',
        letterSpacing: '0.8px',
        color: '#999',
        fontWeight: '600',
        marginBottom: '4px',
    },
    infoValue: {
        fontSize: '15px',
        color: '#1a1a1a',
        lineHeight: '1.5',
    },
    loadingSpinner: {
        width: '40px',
        height: '40px',
        border: '3px solid #f3f3f3',
        borderTop: '3px solid #666',
        borderRadius: '50%',
        animation: 'spin 1s linear infinite',
    },
    errorCard: {
        backgroundColor: '#ffffff',
        borderRadius: '8px',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        maxWidth: '500px',
        width: '100%',
        padding: '40px',
        textAlign: 'center',
    },
    errorTitle: {
        fontSize: '20px',
        fontWeight: '600',
        color: '#1a1a1a',
        margin: '0 0 12px 0',
    },
    errorMessage: {
        fontSize: '15px',
        color: '#666',
        margin: 0,
    },
};

export default RestaurantDetails;
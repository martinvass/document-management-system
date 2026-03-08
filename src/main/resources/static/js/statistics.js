Chart.defaults.font.family = "'Segoe UI', sans-serif";
Chart.defaults.color = '#7A8471';

const COLORS = {
    primary: '#8B9D83',
    secondary: '#C4D4B8',
    accent: '#F5F0E8',
    success: '#90D7A0',
    warning: '#FFE082',
    danger: '#F5A3A8',
    info: '#7DD3DE',
    gradient: ['#8B9D83', '#C4D4B8', '#A8B89E', '#F5F0E8', '#FEFAE0']
};

document.addEventListener('DOMContentLoaded', function() {
    loadDocumentTrends();
    loadFileTypes();
    loadTopUploaders();
    loadDocumentsByDepartment();

    loadTeamGrowth();
    loadDepartmentMembers();
});

async function loadDocumentTrends() {
    const ctx = document.getElementById('documentTrendsChart');
    if (!ctx) return;

    try {
        const response = await fetch('/statistics/api/document-trends');
        const data = await response.json();

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: data.label,
                    data: data.data,
                    borderColor: COLORS.primary,
                    backgroundColor: COLORS.primary + '20',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointBackgroundColor: COLORS.primary,
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        padding: 12,
                        cornerRadius: 8
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        },
                        grid: {
                            color: COLORS.accent
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading document trends:', error);
        showChartError(ctx, 'Failed to load chart data');
    }
}

async function loadFileTypes() {
    const ctx = document.getElementById('fileTypesChart');
    if (!ctx) return;

    try {
        const response = await fetch('/statistics/api/file-types');
        const data = await response.json();

        const colorMap = {
            'Images': '#8B9D83',
            'PDF': '#F5A3A8',
            'Word': '#A4C2F4',
            'Excel': '#A8E6B8',
            'PowerPoint': '#FFBC80',
            'Text': '#B8BFC4',
            'CSV': '#85E3C8',
            'Other': '#C4D4B8'
        };

        const colors = data.labels.map(label => colorMap[label] || '#C4D4B8');

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.data,
                    backgroundColor: colors,
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 15,
                            font: {
                                size: 12
                            },
                            usePointStyle: true,
                            pointStyle: 'circle'
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.parsed || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${label}: ${value} files (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading file types:', error);
        showChartError(ctx, 'Failed to load chart data');
    }
}

async function loadTopUploaders() {
    const ctx = document.getElementById('topUploadersChart');
    if (!ctx) return;

    try {
        const response = await fetch('/statistics/api/top-uploaders');
        const data = await response.json();

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: data.label,
                    data: data.data,
                    backgroundColor: COLORS.secondary,
                    borderColor: COLORS.primary,
                    borderWidth: 1,
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        },
                        grid: {
                            color: COLORS.accent
                        }
                    },
                    y: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading top uploaders:', error);
        showChartError(ctx, 'Failed to load chart data');
    }
}

async function loadDocumentsByDepartment() {
    const ctx = document.getElementById('docsByDepartmentChart');
    if (!ctx) return;

    try {
        const response = await fetch('/statistics/api/documents-by-department');
        const data = await response.json();

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: data.label,
                    data: data.data,
                    backgroundColor: COLORS.primary,
                    borderColor: COLORS.accent,
                    borderWidth: 1,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        },
                        grid: {
                            color: COLORS.accent
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading documents by department:', error);
        showChartError(ctx, 'Failed to load chart data');
    }
}

async function loadTeamGrowth() {
    const ctx = document.getElementById('teamGrowthChart');
    if (!ctx) return;

    try {
        const response = await fetch('/statistics/api/team-growth');
        const data = await response.json();

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: data.label,
                    data: data.data,
                    borderColor: COLORS.success,
                    backgroundColor: COLORS.success + '20',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointBackgroundColor: COLORS.success,
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        },
                        grid: {
                            color: COLORS.accent
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading team growth:', error);
        showChartError(ctx, 'Failed to load chart data');
    }
}

async function loadDepartmentMembers() {
    const ctx = document.getElementById('deptMembersChart');
    if (!ctx) return;

    try {
        const response = await fetch('/statistics/api/department-members');
        const data = await response.json();

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: data.label,
                    data: data.data,
                    backgroundColor: COLORS.warning,
                    borderColor: COLORS.primary,
                    borderWidth: 1,
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        },
                        grid: {
                            color: COLORS.accent
                        }
                    },
                    y: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading department members:', error);
        showChartError(ctx, 'Failed to load chart data');
    }
}

function showChartError(canvas, message) {
    const ctx = canvas.getContext('2d');
    canvas.parentElement.innerHTML = `
        <div class="chart-empty">
            <i class="fas fa-exclamation-triangle"></i>
            <p>${message}</p>
        </div>
    `;
}
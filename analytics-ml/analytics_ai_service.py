"""
ParkWise Analytics Chart Generator
Generates essential charts from parkwise_dataset.csv and stores them in PostgreSQL
"""

import pandas as pd
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import seaborn as sns
import psycopg2
from datetime import datetime
import io
import base64
import numpy as np
import json
import os

# Database configuration
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', '5432')),
    'database': os.getenv('DB_NAME', 'iotdb'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', 'postgres'),
}

# Chart styling
plt.style.use('dark_background')
sns.set_palette("husl")

def get_db_connection():
    """Create database connection"""
    return psycopg2.connect(**DB_CONFIG)

def fig_to_base64(fig):
    """Convert matplotlib figure to base64 data URL"""
    buf = io.BytesIO()
    fig.savefig(buf, format='png', bbox_inches='tight', dpi=150, facecolor='#020617')
    buf.seek(0)
    img_base64 = base64.b64encode(buf.read()).decode('utf-8')
    plt.close(fig)
    return f"data:image/png;base64,{img_base64}"

def store_chart_in_db(chart_name, chart_image, chart_data=None, metadata=None):
    """Store chart in analytics_cache table"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        query = """
                INSERT INTO analytics_cache (chart_name, chart_image, chart_data, metadata, generated_at)
                VALUES (%s, %s, %s, %s, NOW())
                ON CONFLICT (chart_name)
                    DO UPDATE SET
                                  chart_image = EXCLUDED.chart_image,
                                  chart_data = EXCLUDED.chart_data,
                                  metadata = EXCLUDED.metadata,
                                  generated_at = NOW() \
                """

        cursor.execute(query, (chart_name, chart_image, chart_data, metadata))
        conn.commit()
        cursor.close()
        conn.close()

        print(f"✅ Stored chart: {chart_name}")
        return True

    except Exception as e:
        print(f"❌ Error storing chart {chart_name}: {e}")
        return False

def load_dataset(file_path='parkwise_dataset.csv'):
    """Load and parse the dataset"""
    try:
        df = pd.read_csv(file_path)
        print(f"✅ Loaded dataset: {len(df)} rows")
        return df
    except Exception as e:
        print(f"❌ Error loading dataset: {e}")
        return None

def generate_occupancy_time_series(df):
    """Generate Occupancy Rate Time Series chart"""
    try:
        # Group by time and district
        df['hour'] = pd.to_datetime(df['time_of_day'], format='%H').dt.hour

        fig, ax = plt.subplots(figsize=(14, 6), facecolor='#020617')
        ax.set_facecolor('#0f172a')

        # Plot line for each district
        colors = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6']
        for district, color in zip(df['district'].unique(), colors):
            district_data = df[df['district'] == district]
            occupancy_by_hour = district_data.groupby('hour')['busyness_level'].mean()
            ax.plot(occupancy_by_hour.index, occupancy_by_hour.values,
                    label=district, color=color, linewidth=2.5, marker='o', markersize=4)

        ax.set_xlabel('Hour of Day', fontsize=12, color='#cbd5e1')
        ax.set_ylabel('Average Busyness Level', fontsize=12, color='#cbd5e1')
        ax.set_title('Parking Occupancy Trends by Hour',
                     fontsize=16, fontweight='bold', color='#f1f5f9', pad=20)
        ax.legend(loc='upper right', framealpha=0.9, facecolor='#1e293b', edgecolor='#475569')
        ax.grid(True, alpha=0.2, linestyle='--')
        ax.tick_params(colors='#94a3b8')
        ax.set_xticks(range(0, 24, 2))

        img_base64 = fig_to_base64(fig)

        # Store in database
        metadata = json.dumps({"districts": df["district"].unique().tolist(), "hours": 24})
        store_chart_in_db('occupancy_time_series', img_base64, metadata=metadata)

    except Exception as e:
        print(f"❌ Error generating occupancy time series: {e}")

def generate_heatmap_hour_day(df):
    """Generate Heatmap: Occupancy by Hour and Day"""
    try:
        # Create pivot table
        df['hour'] = pd.to_datetime(df['time_of_day'], format='%H').dt.hour

        # Aggregate by hour and day of week
        pivot_data = df.groupby(['day_of_week', 'hour'])['busyness_level'].mean().reset_index()
        pivot_table = pivot_data.pivot(index='hour', columns='day_of_week', values='busyness_level')

        # Reorder columns (Monday=1 to Sunday=7)
        day_names = {0: 'Sun', 1: 'Mon', 2: 'Tue', 3: 'Wed', 4: 'Thu', 5: 'Fri', 6: 'Sat'}
        pivot_table.columns = [day_names.get(col, col) for col in pivot_table.columns]

        fig, ax = plt.subplots(figsize=(12, 10), facecolor='#020617')

        sns.heatmap(
            pivot_table,
            annot=True,
            fmt='.1f',
            cmap='RdYlGn_r',
            cbar_kws={'label': 'Average Busyness Level'},
            linewidths=0.5,
            linecolor='#1e293b',
            ax=ax
        )

        ax.set_xlabel('Day of Week', fontsize=12, color='#cbd5e1', fontweight='bold')
        ax.set_ylabel('Hour of Day', fontsize=12, color='#cbd5e1', fontweight='bold')
        ax.set_title('Parking Occupancy Heatmap: Hour vs Day of Week',
                     fontsize=16, fontweight='bold', color='#f1f5f9', pad=20)

        plt.xticks(rotation=0, color='#94a3b8')
        plt.yticks(rotation=0, color='#94a3b8')

        img_base64 = fig_to_base64(fig)

        # Store in database
        metadata = json.dumps({"type": "heatmap", "dimensions": "hour_day"})
        store_chart_in_db('heatmap_hour_day', img_base64, metadata=metadata)

    except Exception as e:
        print(f"❌ Error generating heatmap: {e}")

def generate_bar_chart_by_location(df):
    """Generate Bar Chart: Average Occupancy by Location"""
    try:
        # Calculate average busyness per district
        avg_by_district = df.groupby('district')['busyness_level'].mean().sort_values(ascending=True)

        fig, ax = plt.subplots(figsize=(10, 6), facecolor='#020617')
        ax.set_facecolor('#0f172a')

        # Color bars based on occupancy level
        colors = ['#10b981' if x < 1.5 else '#f59e0b' if x < 2.5 else '#ef4444'
                  for x in avg_by_district.values]

        bars = ax.barh(avg_by_district.index, avg_by_district.values, color=colors, alpha=0.8)

        # Add value labels on bars
        for bar in bars:
            width = bar.get_width()
            ax.text(width + 0.05, bar.get_y() + bar.get_height()/2,
                    f'{width:.2f}', ha='left', va='center',
                    color='#f1f5f9', fontweight='bold', fontsize=10)

        ax.set_xlabel('Average Busyness Level', fontsize=12, color='#cbd5e1')
        ax.set_ylabel('District', fontsize=12, color='#cbd5e1')
        ax.set_title('Average Parking Busyness by District',
                     fontsize=16, fontweight='bold', color='#f1f5f9', pad=20)
        ax.grid(True, alpha=0.2, linestyle='--', axis='x')
        ax.tick_params(colors='#94a3b8')
        ax.set_xlim(0, 3)

        img_base64 = fig_to_base64(fig)

        # Store in database
        metadata = json.dumps({"districts": len(avg_by_district)})
        store_chart_in_db('bar_chart_by_location', img_base64, metadata=metadata)

    except Exception as e:
        print(f"❌ Error generating bar chart: {e}")

def generate_weather_impact_scatter(df):
    """Generate Weather Impact Analysis (Scatterplot)"""
    try:
        fig, ax = plt.subplots(figsize=(12, 8), facecolor='#020617')
        ax.set_facecolor('#0f172a')

        # Color by busyness level
        colors_map = {0: '#10b981', 1: '#3b82f6', 2: '#f59e0b', 3: '#ef4444'}
        labels_map = {0: 'Very Low', 1: 'Low', 2: 'Moderate', 3: 'High'}

        for level in sorted(df['busyness_level'].unique()):
            level_data = df[df['busyness_level'] == level]
            ax.scatter(level_data['temperature_c'], level_data['precipitation_mm'],
                       c=colors_map[level], alpha=0.6, s=80,
                       label=labels_map[level], edgecolors='white', linewidth=0.5)

        # Add trend line
        z = np.polyfit(df['temperature_c'], df['precipitation_mm'], 1)
        p = np.poly1d(z)
        ax.plot(df['temperature_c'].sort_values(),
                p(df['temperature_c'].sort_values()),
                "r--", alpha=0.5, linewidth=2, label='Trend')

        ax.set_xlabel('Temperature (°C)', fontsize=12, color='#cbd5e1')
        ax.set_ylabel('Precipitation (mm)', fontsize=12, color='#cbd5e1')
        ax.set_title('Weather Impact on Parking Busyness',
                     fontsize=16, fontweight='bold', color='#f1f5f9', pad=20)
        ax.legend(loc='upper right', framealpha=0.9, facecolor='#1e293b', edgecolor='#475569')
        ax.grid(True, alpha=0.2, linestyle='--')
        ax.tick_params(colors='#94a3b8')

        img_base64 = fig_to_base64(fig)

        # Store in database
        metadata = json.dumps({"type": "scatter", "features": ["temperature", "precipitation", "busyness"]})
        store_chart_in_db('weather_impact_scatter', img_base64, metadata=metadata)

    except Exception as e:
        print(f"❌ Error generating weather scatter: {e}")

def generate_busyness_distribution(df):
    """Generate Busyness Level Distribution chart (bonus)"""
    try:
        fig, ax = plt.subplots(figsize=(10, 6), facecolor='#020617')
        ax.set_facecolor('#0f172a')

        busyness_counts = df['busyness_level'].value_counts().sort_index()
        colors = ['#10b981', '#3b82f6', '#f59e0b', '#ef4444']
        labels = ['Very Low (0)', 'Low (1)', 'Moderate (2)', 'High (3)']

        bars = ax.bar(busyness_counts.index, busyness_counts.values,
                      color=colors[:len(busyness_counts)], alpha=0.8)

        # Add value labels
        for bar in bars:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                    f'{int(height)}', ha='center', va='bottom',
                    color='#f1f5f9', fontweight='bold', fontsize=12)

        ax.set_xlabel('Busyness Level', fontsize=12, color='#cbd5e1')
        ax.set_ylabel('Number of Observations', fontsize=12, color='#cbd5e1')
        ax.set_title('Distribution of Parking Busyness Levels',
                     fontsize=16, fontweight='bold', color='#f1f5f9', pad=20)
        ax.set_xticks(range(len(busyness_counts)))
        ax.set_xticklabels(labels[:len(busyness_counts)])
        ax.grid(True, alpha=0.2, linestyle='--', axis='y')
        ax.tick_params(colors='#94a3b8')

        img_base64 = fig_to_base64(fig)

        # Store in database
        metadata = json.dumps({"total_observations": len(df)})
        store_chart_in_db('busyness_distribution', img_base64, metadata=metadata)

    except Exception as e:
        print(f"❌ Error generating busyness distribution: {e}")

def main():
    """Main execution function"""
    print("=" * 60)
    print("🚀 ParkWise Analytics Chart Generator")
    print("=" * 60)

    # Load dataset
    df = load_dataset('parkwise_dataset.csv')
    if df is None:
        print("❌ Failed to load dataset. Exiting.")
        return

    print(f"\n📊 Dataset shape: {df.shape}")
    print(f"📋 Columns: {list(df.columns)}")
    print(f"🏙️  Districts: {df['district'].unique()}")
    print(f"⏰ Time range: {df['time_of_day'].min()} - {df['time_of_day'].max()}")

    print("\n" + "=" * 60)
    print("Generating charts...")
    print("=" * 60 + "\n")

    # Generate all essential charts
    generate_occupancy_time_series(df)
    generate_heatmap_hour_day(df)
    generate_bar_chart_by_location(df)
    generate_weather_impact_scatter(df)
    generate_busyness_distribution(df)

    print("\n" + "=" * 60)
    print("✅ All charts generated and stored in database!")
    print("=" * 60)
    print("\n📌 Charts stored in 'analytics_cache' table:")
    print("   - occupancy_time_series")
    print("   - heatmap_hour_day")
    print("   - bar_chart_by_location")
    print("   - weather_impact_scatter")
    print("   - busyness_distribution")
    print("\n🔄 Spring Boot can now fetch these charts from the database")
    print("=" * 60)

if __name__ == "__main__":
    main()
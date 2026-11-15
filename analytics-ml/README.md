# ParkWise analytics and prediction

- `model.ipynb`: data analysis and training of a Random Forest classifier that predicts parking busyness (0-3) from district, day, time and weather, using `parkwise_dataset.csv`. Hyperparameters are tuned with `GridSearchCV`.
- `random_forest_model.pkl`, `X_columns.pkl`: the trained model and its feature columns.
- `model_api.py`: FastAPI service. `POST /predict_all {"time_of_day": 8}` returns a prediction per Antwerp district, using live weather from weatherapi.com (`WEATHER_API_KEY`).
- `analytics_ai_service.py`: generates the dashboard charts and stores them in the `analytics_cache` table.

```bash
pip install -r requirements.txt
python analytics_ai_service.py
uvicorn model_api:app --port 8000
```

Database settings: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

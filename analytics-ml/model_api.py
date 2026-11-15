import joblib
import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel
import requests
from datetime import datetime
import os

model = joblib.load("random_forest_model.pkl")
X_columns = joblib.load("X_columns.pkl")

app = FastAPI(title="Parkwise Busyness Prediction API")

class PredictRequest(BaseModel):
    time_of_day: int

DISTRICTS = ["Historic Center", "Zuid", "Noord", "Borgerhout"]

WEATHER_API_KEY = os.getenv("WEATHER_API_KEY", "")  # free key from weatherapi.com
CITY = "Antwerp"
WEATHER_API_URL = f"http://api.weatherapi.com/v1/current.json?key={WEATHER_API_KEY}&q={CITY}"

@app.post("/predict_all")
def predict_all(data: PredictRequest):
    now = datetime.now()
    day_of_week = now.isoweekday()

    weather_resp = requests.get(WEATHER_API_URL)
    weather_json = weather_resp.json()
    current = weather_json["current"]

    temperature_c = current["temp_c"]
    humidity_pct = current["humidity"]
    rainfall_mm = current.get("precip_mm", 0.0)
    wind_speed_kmh = current["wind_kph"]

    predictions = {}
    for district in DISTRICTS:
        row = {
            "district": district,
            "day_of_week": day_of_week,
            "time_of_day": data.time_of_day,
            "temperature_c": temperature_c,
            "precipitation_mm": rainfall_mm,  # the model was trained on this column name
            "wind_speed_kmh": wind_speed_kmh
        }

        df = pd.DataFrame([row])
        df = pd.get_dummies(df, columns=["district"])
        df = df.reindex(columns=X_columns, fill_value=0)

        pred = int(model.predict(df)[0])
        predictions[district] = pred

    return {"predictions": predictions}
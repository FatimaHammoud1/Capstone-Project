from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import joblib

# Load artifacts
model = joblib.load("model_artifacts/svm_model.pkl")
scaler = joblib.load("model_artifacts/scaler.pkl")
feature_columns = joblib.load("model_artifacts/feature_columns.pkl")
scaling_cols = joblib.load("model_artifacts/scaling_cols.pkl")
mapping = joblib.load("model_artifacts/mapping.pkl")
label_encoder = joblib.load("model_artifacts/label_encoder.pkl")

app = FastAPI(title="ML Personality Classification Service")

class PredictionRequest(BaseModel):
    answers: dict  # {"Q1": "نعم", "Q2": "لا", ...}

@app.post("/api/ml/predict-code")
def predict_code(request: PredictionRequest):
    df = pd.DataFrame([request.answers])

    # Map answers
    for col in df.columns:
        df[col] = df[col].map(mapping).fillna(0)

    # Ensure column order
    df = df.reindex(columns=feature_columns, fill_value=0)

    # Scale
    df.loc[:, scaling_cols] = scaler.transform(df[scaling_cols])

    # Predict
    pred_encoded = model.predict(df)[0]
    pred_code = label_encoder.inverse_transform([pred_encoded])[0]

    return {
        "predictedCode": pred_code
    }

# to run : uvicorn ml_service:app --host 0.0.0.0 --port 5001 --reload
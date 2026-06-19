from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="AngriSmart Backend API",
    description="API for Rice Farming Mobile App",
    version="1.0.0"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from app.api.v1 import disease
from app.api.v1 import chat
app.include_router(disease.router, prefix="/api/v1/disease", tags=["AI Image Scanning"])
app.include_router(chat.router, prefix="/api/v1/chat", tags=["Expert Chatbot"])

@app.get("/")
def read_root():
    return {"message": "Welcome to AngriSmart ML API is online!"}

# Example route import
# from app.api.v1 import route_manager
# app.include_router(route_manager.router, prefix="/api/v1")

from fastapi import FastAPI
from presidio_analyzer import AnalyzerEngine
from presidio_anonymizer import AnonymizerEngine
from pydantic import BaseModel
import uvicorn

app = FastAPI(title="Sentinel NER Service")

# Initialize Presidio
analyzer = AnalyzerEngine()
anonymizer = AnonymizerEngine()

class DetectRequest(BaseModel):
    text: str
    tenant_id: str

@app.post("/analyze")
async def analyze_text(request: DetectRequest):
    # Analyze for PII
    results = analyzer.analyze(text=request.text, language='en')
    
    # Anonymize (Redact) the text
    anonymized_result = anonymizer.anonymize(
        text=request.text,
        analyzer_results=results
    )
    
    entities = [
        {
            "type": result.entity_type,
            "start": result.start,
            "end": result.end,
            "score": result.score
        }
        for result in results
    ]
    
    return {
        "entities": entities,
        "redacted_text": anonymized_result.text
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)

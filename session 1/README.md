<h1>Session 1 – UDP Measurements & Telemetry Collection</h1>

<h3>Session 1 includes the first complete set of measurements for:</h3>

- Echo packets (with and without delay)
- Camera image retrieval
- Temperature sensor packets
- DPCM and AQ-DPCM audio extraction
- UAV telemetry (IthakiCopter)
- Vehicle OBD-II sensor decoding

<h2>Contents</h2>

```
session1/
├── audio/
│   ├── generator_DPCM_Samples
│   ├── generator_DPCM_Subs
│   ├── song1_DPCM_Samples
│   ├── song1_DPCM_Subs
│   ├── song1_AQ-DPCM_Samples
│   ├── song1_AQ-DPCM_Subs
│   ├── song1_AQ-DPCM_M
│   ├── song1_AQ-DPCM_B
│   ├── song2_DPCM_Samples
│   ├── song2_DPCM_Subs
│   ├── song2_AQ-DPCM_Samples
│   ├── song2_AQ-DPCM_Subs
│   ├── song2_AQ-DPCM_M
│   └── song2_AQ-DPCM_B
│ 
│
├── data/
│   ├── E####_DELAY.txt
│   ├── E####_NO_DELAY.txt
│   ├── ITHAKICOPTER.json
│   ├── V0103.json
│   ├── generator_DPCM_Samples
│   └── generator_DPCM_Subs
│   
│
├── plots/
│   └── *.png   ← MATLAB plots (G1–G21)
│
├── parameters.txt
└── README.md
```

<h2>MATLAB Analysis (Session 1)</h2>

<h3>The MATLAB script produces:</h3>

- RTT vs time plots (G1, G3)
- Throughput over 8-second windows (G2, G4)
- RTT histograms (G5–G8)
- RTO/SRTT/σ estimation (R1)
- DPCM & AQ-DPCM waveform and histogram analysis (G9–G18)
- UAV telemetry plots (G19–G20)
- Vehicle OBD-II sensor plots (G21)

<h3>RTT Statistical Metrics</h3>

**Documented inside `parameters.txt`:**

- **Mean RTT:** 1.66 × 10³ ms  
- **Variance:** 1.08 × 10⁵  
- **Adaptive filtering parameters:**  
  - a = 0.8  
  - bt = 0.9  
  - c = 3  

These parameters were used in MATLAB for SRTT, σ, and RTO estimation.


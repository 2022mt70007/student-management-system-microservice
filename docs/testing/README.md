# Security Testing — Separate Sheets per Test Type

Each architecture has **8 sheets** (one CSV per sheet).

## Monolith-Testing.xlsx
Import all files from `monolith/` folder:

| Sheet name in Excel | CSV file |
|---------------------|----------|
| 0 Setup | 0-Setup.csv |
| 1 Response Time | 1-Response-Time-Comparison.csv |
| 2 Broken Access Control | 2-Broken-Access-Control.csv |
| 3 JWT Tampering | 3-JWT-Tampering.csv |
| 4 OWASP Top 10 | 4-OWASP-Top10.csv |
| 5 Load Testing | 5-Load-Testing.csv |
| 6 Penetration Testing | 6-Penetration-Testing.csv |
| 7 Metrics Summary | 7-Metrics-Summary.csv |

**URL:** https://veritascampus.page

## Microservices-Testing.xlsx
Import all files from `microservices/` folder (same 8 sheet names).

**URL:** https://veritascampus.me

## Comparison-Report.xlsx
Import `8-Mono-vs-MS-Comparison.csv` after both workbooks are done.

## Professor feedback mapping

| Sheet | Professor item |
|-------|----------------|
| 1 Response Time | Response time comparison |
| 2 Broken Access Control | Broken access control |
| 3 JWT Tampering | JWT tampering |
| 4 OWASP Top 10 | OWASP Top 10 assessment |
| 5 Load Testing | Load testing |
| 6 Penetration Testing | Penetration testing (findings) |
| 7 Metrics Summary | Aggregated before/after metrics |
| 8 Comparison | Microservices vs monolith comparison |

## Before / After

Sheets 1–5 have BEFORE and AFTER columns.
- Fill BEFORE when you first run the test
- Fill AFTER only if test failed or after applying a fix
- Sheet 7 summarizes metrics from all sheets

## Import steps in Excel

1. New workbook
2. Data → Get Data → From Text/CSV → pick first CSV → Load
3. Rename sheet (e.g. "1 Response Time")
4. Repeat for each CSV in order 0 to 7
5. Save as Monolith-Testing.xlsx

## Test order

0 Setup → 1 Response Time → 2 RBAC → 3 JWT → 4 OWASP → 5 Load → 6 Pentest findings → 7 Metrics

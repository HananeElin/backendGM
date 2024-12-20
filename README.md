# Excel File Processor API

This project is a Spring Boot-based API that allows users to upload, process, and normalize Excel (`.xlsx`) files.
It validates the data in the file, categorizes rows into valid and invalid sets, and provides the processed results for download as a `.zip` file containing two Excel files.

---

## Features

- **Excel File Upload**: Accepts `.xlsx` files via REST API.
- **Data Validation**:
  - **Phone Numbers**: Normalizes formats (e.g., converting `+212` to `0`) and ensures uniqueness.
  - **Amounts**: Converts monetary values to integers.
  - **Registration Numbers**: Validates format and structure, ensuring compliance with specific rules.
- **Row Categorization**: Separates valid and invalid rows into different sheets.
- **Downloadable Results**: Returns the processed data in a `.zip` containing:
  - A file for valid rows.
  - A file for invalid rows with reasons for invalidity.

---

## Technologies Used

- **Java 21**: Core programming language.
- **Spring Boot 3.3.6**: Framework for building the RESTful API.
- **Apache POI 4.1.2**: Library for handling Excel files.
- **Maven**: Dependency and build management tool.
- **Lombok**: Simplifies Java development with automatic getters/setters and more.

---

## Prerequisites

Ensure you have the following installed on your system:

- **Java 21+**
- **Maven 3.6+**
- **Postman** (or any other API testing tool)

---

## Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/HananeElin/backendGM.git
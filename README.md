🧠 Incident Resolution System (Final Year Project)
This is the source code for my Final Year Project:
"Intelligent Incident Resolution System using Machine Learning and NLP Techniques for Enhanced Support Efficiency"

The system allows users to submit IT/HR/Support-related incidents. Admins can view and resolve them, with a powerful built-in semantic similarity feature that compares new incidents to previously resolved ones using a hybrid approach (TF-IDF + GloVe embeddings).

🔧 Requirements
- Java 17

- Spring Boot

- Maven

- MySQL (via XAMPP & phpMyAdmin)

- GloVe 50d Binary Embedding File

Git (for cloning, optional)

📁 Project Setup Instructions
1. Download Source Code from this repository

2. Set Up MySQL Database with XAMPP
Launch XAMPP and start Apache and MySQL.

Open phpMyAdmin and create a new databse named fyp_incident_system , the SQL script can be found under database-setup.txt:
This file is included in the project directory.


3. Update Application Properties
Ensure the following database credentials are set in src/main/resources/application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/fyp_incident_system
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

4. GloVe Model Setup
The project requires a pretrained GloVe embedding file:

- File name: `glove.6B.50d.bin`
- Download it from this link: [Download from Google Drive]  (https://tusmm-my.sharepoint.com/:f:/g/personal/k00261195_student_tus_ie/Eqj4X03Ogq5Gu9bVd9lhTJkB7TJXIzdIyOq9oDgt72t_Mg?e=hZgO6k)

After downloading, you will need to create a folder in your C: Drive named models, and place the GloVe binary file in this models folder.

⚠️ This file path is hardcoded, so please follow the folder structure exactly.


5. Login Credentials
You can log in using the following test accounts:

Role	Username	Password
Admin	admin	admin123
User	user	user123

💡 Key Features
- Incident submission for users

- Admin dashboard with role-based access

- Semantic similarity search powered by:

    - TF-IDF cosine similarity

    - GloVe 50D embeddings + cosine comparison

- Match results include title, similarity %, and resolution

- Preprocessing: stopword removal + domain-specific synonym expansion

- Modular architecture (Spring Boot)

📌 Notes
If the Word2Vec binary file is not found or the model isn't loaded correctly, the similarity feature will not function.

The synonyms used during preprocessing are hardcoded in the service class.

📷 Demo
This system was presented as part of my Final Year Project demo and evaluated by academic supervisors.

📬 Contact
If you have any issues setting up or running this project, feel free to contact me at:
adammc3011@gmail.com

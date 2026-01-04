### **2. Teste de Validação de Dados Inexistentes**

* **O que é testado:** A robustez da procedure ao lidar com identificadores (IDs) que não constam na base de dados.

**Como funciona:**
* O bloco tenta associar um ID de frete fictício (ex: `99999`) a um comboio válido.
* A procedure realiza uma verificação prévia através de um `SELECT COUNT`.
* O teste é
**1. Teste de Inserção com Sucesso**

* **O que é testado:** A capacidade do sistema de registar uma nova instalação (Facility) fornecendo um nome válido e recebendo o ID gerado manualmente através da lógica de MAX(id) + 1

**Como funciona:**
* O bloco define um nome de teste único.
* Invoca a função `fn_register_facility` e armazena o ID de retorno.
* Realiza um `SELECT` à base de dados filtrando pelo ID retornado.
* O teste é bem-sucedido se o nome guardado na base de dados corresponder exatamente ao nome enviado no input.
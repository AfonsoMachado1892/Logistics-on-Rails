**5. Teste de Validação de Inputs Nulos  ou Vazios**

* **O que é testado:** A capacidade da função de identificar e rejeitar strings que, embora não sejam nulas, não contêm caracteres válidos (apenas espaços em branco).

**Como funciona:**
* O bloco envia uma string composta por vários espaços (ex: ' ').
* A função utiliza o TRIM para verificar se existe conteúdo real após remover os espaços.
* O teste é validado se o sistema mandar o erro -20001, impedindo a gravação de um nome vazio na base de dados.
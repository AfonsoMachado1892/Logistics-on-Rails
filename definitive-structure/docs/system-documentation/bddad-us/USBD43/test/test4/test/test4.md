**4. Teste de Duplicados contra Dados Pré-existentes**

* **O que é testado:** A validação de novos registos contra dados que já foram carregados anteriormente no sistema (USBD32).

**Importância:** Verifica se a função de registo respeita a base de dados global e não apenas os dados inseridos na sessão atual. É crucial para manter a consistência após o carregamento inicial de dados da rede.

**Como funciona:**
* O teste verifica primeiro se a estação 'Valença' (carregada via USBD32) existe.
* Tenta inserir novamente '  VALENÇA  '.
* O teste é considerado SUCESSO se o sistema identificar o conflito com os dados históricos e impedir a inserção duplicada.
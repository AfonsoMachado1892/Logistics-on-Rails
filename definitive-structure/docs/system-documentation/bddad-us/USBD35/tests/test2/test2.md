**2. Teste de Deteção de Duplicados (Case & Spaces)**

* **O que é testado:** A capacidade de identificar nomes duplicados, mesmo quando estes contêm espaços extras ou variações de maiúsculas/minúsculas (Ex: "AVEIRO" vs "aveiro").

**Como funciona:**
* Insere um registo base.
* Tenta inserir imediatamente a seguir uma variante propositadamente alterada (ex: '  AVEIRO   TEST   NODE  ').
* O teste é um SUCESSO se o sistema lançar o erro customizado `-20002`, indicando que o duplicado foi corretamente bloqueado.
**2. Teste de Deteção de Duplicados (Case & Spaces)**

* **O que é testado:** A capacidade de identificar nomes duplicados, mesmo quando estes contêm espaços extras ou variações de maiúsculas/minúsculas (Ex: "AVEIRO" vs "aveiro").

**Importância:** Garante a integridade dos dados e evita redundância na rede ferroviária. O sistema deve normalizar o input antes da comparação para garantir que a mesma estação não seja registada duas vezes.

**Como funciona:**
* Insere um registo base (ex: 'Aveiro Test Node').
* Tenta inserir imediatamente a seguir uma variante propositadamente "suja" (ex: '  AVEIRO   TEST   NODE  ').
* O teste é um SUCESSO se o sistema lançar o erro customizado `-20002`, indicando que o duplicado foi corretamente bloqueado.
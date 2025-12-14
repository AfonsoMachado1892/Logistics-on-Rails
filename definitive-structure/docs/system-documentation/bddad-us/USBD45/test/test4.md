**Test 4: Validação de regras de negócio e valores de atributos**

> Testes realizados:
> - Mesma facility início/fim → Erro: "Start and end facilities must be different"
> - Eletrificação inválida → Erro: "Electrified must be 'Yes' or 'No'"
> - Número de tracks inválido → Erro: "Number of tracks must be 1 or 2"
> - Comprimento zero → Erro: "Segment length must be greater than 0"
> - Comprimento negativo → Erro: "Segment length must be greater than 0"
> - Peso máximo negativo → Erro: "Max weight per meter must be greater than 0"
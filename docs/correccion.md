
# Informe de Corrección

---

## 1. Definiciones Formales del Dominio

Sea $\mathcal{S}$ el conjunto de todos los registros sísmicos posibles, donde cada sismo $s \in \mathcal{S}$ se modela como una tupla:

$$s = (f, \text{reg}, m, p) \in \text{Fecha} \times \text{String} \times \mathbb{R} \times \mathbb{Z}$$

Donde las proyecciones de cada elemento están dadas por:
* $\pi_{\text{reg}}(s) = s.\text{region}$
* $\pi_{m}(s) = s.\text{magnitud}$

Sea $R = [s_1, s_2, \ldots, s_n]$ una lista finita de sismos ($R \in \text{List}[\mathcal{S}]$), y sea $C_R = \{s_1, s_2, \ldots, s_n\}$ el multiconjunto asociado a los elementos de $R$.

---

## 2. Corrección de las Funciones Implementadas

A diferencia de los programas imperativos o recursivos explícitos, las funciones de este taller están formuladas mediante **especificaciones declarativas** (comprensiones `for`, transformaciones sobre mapas y evaluación perezosa), donde la implementación coincide directamente con la definición matemática de los conjuntos y relaciones buscadas.

### 2.1. Punto 1: Consultas con expresiones `for`

#### a) `fuertes(r, umbral)`
* **Especificación matemática:**
  $$f_{\text{fuertes}}(R, u) = \left[ \pi_{\text{reg}}(s) \;\middle|\; s \in R \land \pi_m(s) > u \right]$$
* **Argumento de corrección:** La expresión `for { s <- r if s.magnitud > umbral } yield s.region` se traduce en una operación de filtrado (`withFilter`) seguida de una proyección (`map`). Por definición, conserva todos y únicamente los elementos $s \in R$ que satisfacen el predicado $P(s) \equiv \pi_m(s) > u$, manteniendo sus multiplicidades y proyectando su atributo de región.

#### b) `enRegion(r, region)`
* **Especificación matemática:**
  $$f_{\text{enRegion}}(R, \rho) = \left[ \pi_m(s) \;\middle|\; s \in R \land \pi_{\text{reg}}(s) = \rho \right]$$
* **Argumento de corrección:** El filtro selecciona el subconjunto de eventos cuya región coincide exactamente con $\rho$, y `yield s.magnitud` aplica la proyección $\pi_m$ sobre cada elemento filtrado. Si ningún elemento cumple $\pi_{\text{reg}}(s) = \rho$, el resultado es la lista vacía $[\,]$.

#### c) `paresCercanos(r, dif)`
* **Especificación matemática:** Sea $\mathcal{U}(R) = [\rho_1, \rho_2, \ldots, \rho_k]$ la secuencia de regiones únicas de $R$ según su primera aparición. Definimos el predicado de cercanía entre dos regiones $a, b$:
  $$\text{Cercanos}(a, b, d) \iff \exists x, y \in R : \left( \pi_{\text{reg}}(x) = a \land \pi_{\text{reg}}(y) = b \land |\pi_m(x) - \pi_m(y)| \leq d \right)$$
  Entonces:
  $$f_{\text{pares}}(R, d) = \left[ (\rho_i, \rho_j) \;\middle|\; 0 \leq i < j < k \land \text{Cercanos}(\rho_i, \rho_j, d) \right]$$
* **Argumento de corrección:**
  1. **Sin duplicados ni simétricos:** Al restringir los índices a $0 \leq i < j < k$ sobre la lista sin duplicados $\mathcal{U}(R)$, se garantiza que $\rho_i \neq \rho_j$ (no hay pares de una región consigo misma) y que cada par no ordenado $\{\rho_i, \rho_j\}$ se evalúa una única vez (evitando tanto $(\rho_i, \rho_j)$ como $(\rho_j, \rho_i)$).
  2. **Existencialidad:** La función auxiliar `hayCercanos(a, b)` verifica que la comprensión del producto cartesiano filtrado no sea vacía (`!isEmpty`), lo cual es lógicamente equivalente al cuantificador existencial $\exists x, y$.

---

### 2.2. Punto 2: Agrupar con mapas

#### a) `porRegion(r)`
* **Especificación matemática:** Sea $\text{Dom}(R) = \{ \pi_{\text{reg}}(s) \mid s \in R \}$. La función define un mapeo $M : \text{Dom}(R) \to \text{List}[\mathcal{S}]$ tal que:
  $$\forall \rho \in \text{Dom}(R) : M(\rho) = \left[ t \in R \;\middle|\; \pi_{\text{reg}}(t) = \rho \right]$$
* **Argumento de corrección:** Para cada elemento $s \in R$, se construye el par $(\pi_{\text{reg}}(s), L_{\pi_{\text{reg}}(s)})$, donde $L_{\rho}$ contiene todos los sismos de $R$ pertenecientes a $\rho$. Al aplicar `.toMap`, las claves repetidas colapsan en una única entrada del mapa asociada exactamente al mismo valor $L_\rho$, garantizando que $\text{Dom}(M) = \text{Dom}(R)$ y que ningún elemento fuera de $R$ sea incluido.

#### b) `conteoPorRegion(r)` y `magnitudMaxima(r)`
* **Especificación matemática:**
  $$M_{\text{conteo}}(\rho) = |M(\rho)|, \qquad M_{\text{max}}(\rho) = \max_{s \in M(\rho)} \pi_m(s)$$
* **Argumento de corrección:** Dado que `porRegion(r)` garantiza que para toda clave $\rho \in \text{Dom}(M)$, la lista $M(\rho)$ es no vacía ($|M(\rho)| \geq 1$), la operación `.length` devuelve el cardinal exacto de sismos en $\rho$, y la operación `.max` sobre la proyección de magnitudes está siempre bien definida (sin riesgo de excepción por colección vacía).

---

### 2.3. Punto 3: Consultas seguras

#### a) `conteoSeguro(r)` y `maximaDe(r, region)`
* **Corrección:**
  * `withDefaultValue(0)` extiende la función parcial $M_{\text{conteo}} : \text{Dom}(R) \to \mathbb{N}$ a una función total $M'_{\text{conteo}} : \text{String} \to \mathbb{N}$ definida como:
    $$M'_{\text{conteo}}(\rho) = \begin{cases} M_{\text{conteo}}(\rho) & \text{si } \rho \in \text{Dom}(R) \\ 0 & \text{si } \rho \notin \text{Dom}(R) \end{cases}$$
  * `.get(region)` eleva el codominio de $M_{\text{max}}$ al tipo opción $\text{Option}[\mathbb{R}]$, retornando $\text{Some}(M_{\text{max}}(\rho))$ si $\rho \in \text{Dom}(R)$ y $\text{None}$ en caso contrario, eliminando cualquier fallo en tiempo de ejecución.

#### b) `regionMasActiva(r)`
* **Especificación matemática:** Sea la relación de orden total $\succ$ sobre pares $(\rho, c) \in \text{String} \times \mathbb{N}$ definida por:
  $$(\rho_1, c_1) \succ (\rho_2, c_2) \iff (c_1 > c_2) \lor (c_1 = c_2 \land \rho_1 <_{\text{lex}} \rho_2)$$
* **Argumento de corrección:** Si $R = [\,]$, `conteo` es vacío y retorna `None`. Si $R \neq [\,]$, la relación $\succ$ es irreflexiva, asimétrica y transitiva (un orden total estricto sobre claves únicas). Por tanto, `reduce` converge al único elemento maximal $(\rho^*, c^*)$ respecto a $\succ$, cumpliendo tanto el criterio de mayor frecuencia como el desempate lexicográfico.

---

### 2.4. Punto 4: Réplicas perezosas

#### a) `replicas(magnitud)`
* **Definición por coinducción:** La secuencia infinita $S(m_0) = [a_0, a_1, a_2, \ldots]$ se define mediante la ecuación recurrente:
  $$a_n = \text{redondear}(m_0 \cdot 0.8^n), \quad \forall n \in \mathbb{N}$$
  En el código, `redondear(magnitud) #:: replicas(magnitud * 0.8)` construye la cabeza $a_0 = \text{redondear}(m_0)$ y suspende el cálculo de la cola mediante evaluación por necesidad (*call-by-need*) con el operador `#::`.

#### b) `replicasSensibles(magnitud, minimo)`
* **Argumento de terminación y corrección:**
  $$\text{replicasSensibles}(m_0, \mu) = \left[ a_0, a_1, \ldots, a_{k-1} \right] \quad \text{donde } k = \min \{ n \in \mathbb{N} \mid a_n \leq \mu \}$$
  Como $0 < 0.8 < 1$, para cualquier magnitud inicial $m_0 > 0$ la sucesión geométrica $m_0 \cdot 0.8^n$ es estrictamente decreciente y converge a $0$:
  $$\lim_{n \to \infty} m_0 \cdot 0.8^n = 0$$
  Dado que en la escala sísmica $\mu > 0$ (o para cualquier $\mu \geq 0$ tras redondeo a dos decimales, donde $a_n = 0.0$ para $n$ suficientemente grande), existe un índice finito $k \in \mathbb{N}$ tal que $a_k \leq \mu$. Por tanto, `takeWhile(_ > minimo)` siempre encuentra un elemento que falsifica el predicado en un número finito de pasos, garantizando la terminación de `.toList`.

---

### 2.5. Punto 5: Informe combinado

#### `informe(r, minimo)`
* **Especificación matemática:**
  $$I(R, \mu) = \left\{ \rho \mapsto \text{Resumen}\left(M_{\text{conteo}}(\rho),\; M_{\text{max}}(\rho),\; |\text{replicasSensibles}(M_{\text{max}}(\rho), \mu)|\right) \;\middle|\; \rho \in \text{Dom}(R) \right\}$$
* **Argumento de corrección:** Las estructuras `conteos` y `magnitudMaxima(r)` comparten exactamente el mismo dominio $\text{Dom}(R)$ derivado de `porRegion(r)`. Por ello, para toda clave `reg` en `magnitudMaxima(r)`, la consulta `conteos(reg)` está garantizada dentro del dominio y combina de forma consistente las tres métricas por región.

---

## 3. Invarianza respecto al orden de recorrido

Un aspecto fundamental de la corrección de esta solución es que **los resultados esenciales de las consultas no dependen del orden en que se recorran los elementos del registro**, lo cual se justifica por las siguientes propiedades matemáticas y estructurales:

1. **Conmutatividad y asociatividad de las operaciones de agregación:**
* En `conteoPorRegion`, el tamaño de un conjunto filtrado $| \{ s \in R \mid \pi_{\text{reg}}(s) = \rho \} |$ depende únicamente de la función indicadora de pertenencia a la región $\rho$. La suma de unidades ($1 + 1 + \dots + 1$) es conmutativa y asociativa.
* En `magnitudMaxima`, el operador $\max$ sobre números reales satisface $\max(x, y) = \max(y, x)$ y $\max(x, \max(y, z)) = \max(\max(x, y), z)$. Por lo tanto, el máximo de una región es invariante ante cualquier permutación $\sigma(R)$ de la lista de entrada.

2. **Independencia del orden en estructuras de tipo Mapa (`Map`):**
* Un `Map[K, V]` representa matemáticamente una función finita (un conjunto de pares clave-valor sin orden intrínseco). Dos mapas $M_1$ y $M_2$ son iguales ($M_1 = M_2$) si y solo si tienen el mismo dominio y asignan el mismo valor a cada clave, independientemente del orden en que las claves fueron insertadas o recorridas.
* En `regionMasActiva`, el operador de reducción $\succ$ define un **orden total estricto** (gracias al desempate alfabético $\rho_1 <_{\text{lex}} \rho_2$). Al ser un orden total sobre claves únicas, el elemento máximo es único y el resultado de `reduce` es completamente independiente del orden interno de iteración del mapa.

3. **Simetría en `paresCercanos`:**
* La condición de cercanía $|\pi_m(x) - \pi_m(y)| \leq d$ es simétrica ($|a - b| = |b - a|$), por lo que la existencia de un par de sismos cercanos entre dos regiones $A$ y $B$ es una propiedad del conjunto de eventos y no de su ordenamiento secuencial. *(Nota: únicamente el orden de presentación de las listas de salida en el Punto 1 y la orientación de la tupla $(\rho_i, \rho_j)$ reflejan el orden de aparición solicitado explícitamente por el enunciado).*

4. **Ausencia de estado mutable y efectos secundarios:**
* Todas las funciones son **puras** (transparencia referencial): no existen acumuladores mutables (`var`) ni ciclos imperativos cuyo estado intermedio dependa del historial de iteración. Cada transformación opera sobre colecciones inmutables, garantizando determinismo total ante cualquier estrategia de evaluación.
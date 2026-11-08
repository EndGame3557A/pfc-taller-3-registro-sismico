package taller

/**
 * Taller 3 — consultas sobre un registro sísmico con expresiones for, mapas y
 * secuencias perezosas.
 *
 * Complete cada función. Las firmas y los tipos no se modifican.
 */
class RegistroSismico {

  type Registro = List[Sismo]

  // Punto 1: consultas con expresiones for --------------------------------------

  /** Las regiones de los sismos que superan el umbral, en orden y con repetidos. */
  def fuertes(r: Registro, umbral: Double): List[String] = 
    for { s <- r if s.magnitud > umbral } yield s.region

  /** Las magnitudes registradas en una región. */
  def enRegion(r: Registro, region: String): List[Double] =
    for { s <- r if s.region == region } yield s.magnitud


  /** Parejas de regiones distintas con algún sismo de magnitudes cercanas. */
  def paresCercanos(r: Registro, dif: Double): List[(String, String)] = {
    val regiones = (for (s <- r) yield s.region).distinct
    def hayCercanos(a: String, b: String): Boolean =
      !(for {
        x <- r if x.region == a
        y <- r if y.region == b
        if Math.abs(x.magnitud - y.magnitud) <= dif
      } yield true).isEmpty
    (for {
      i <- 0 until regiones.length
      j <- (i + 1) until regiones.length
      if hayCercanos(regiones(i), regiones(j))
    } yield (regiones(i), regiones(j))).toList
  }

  // Punto 2: agrupar con mapas --------------------------------------------------

  def porRegion(r: Registro): Map[String, List[Sismo]] = 
    (for (s <- r)
    yield (s.region, for (t <- r if t.region == s.region) yield t)).toMap

  def conteoPorRegion(r: Registro): Map[String, Int] = 
    porRegion(r).map { case (reg, ss) => (reg, ss.length) }

  def magnitudMaxima(r: Registro): Map[String, Double] = 
    porRegion(r).map { case (reg, ss) => (reg, (for (s <- ss) yield s.magnitud).max) }

  // Punto 3: consultas seguras --------------------------------------------------

  /** Un mapa que responde cero por las regiones que no están en el registro. */
  def conteoSeguro(r: Registro): Map[String, Int] = 
    conteoPorRegion(r).withDefaultValue(0)

  /** La magnitud máxima de una región, o vacío si no hay registros suyos. */
  def maximaDe(r: Registro, region: String): Option[Double] = 
    magnitudMaxima(r).get(region)

  /** La región con más sismos; en empate, la primera en orden alfabético. */
  def regionMasActiva(r: Registro): Option[String] = {
    val conteo = conteoPorRegion(r)
    if (conteo.isEmpty) None
    else Some(conteo.reduce((a, b) =>
      if (a._2 > b._2 || (a._2 == b._2 && a._1 < b._1)) a else b)._1)
  }

  // Punto 4: réplicas perezosas -------------------------------------------------

  def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0

  /** La secuencia infinita que empieza en la magnitud y multiplica por 0.8. */
  def replicas(magnitud: Double): LazyList[Double] = 
    redondear(magnitud) #:: replicas(magnitud * 0.8)

  /** Las réplicas mientras superen el mínimo, incluida la primera. */
  def replicasSensibles(magnitud: Double, minimo: Double): List[Double] = 
    replicas(magnitud).takeWhile(_ > minimo).toList

  // Punto 5: informe combinado --------------------------------------------------

  /** Conteo, magnitud máxima y réplicas esperadas del sismo más fuerte, por región. */
  def informe(r: Registro, minimo: Double): Map[String, Resumen] = {
    val conteos = conteoPorRegion(r)
    magnitudMaxima(r).map { case (reg, maxima) =>
      (reg, Resumen(conteos(reg), maxima, replicasSensibles(maxima, minimo).length))
    }
  }
}
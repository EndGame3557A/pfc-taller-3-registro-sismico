package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Datos.registro

@RunWith(classOf[JUnitRunner])
class RegistroSismicoTest extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  val vacio: List[Sismo] = List()

  // Punto 1 -------------------------------------------------------------------

  test("fuertes: los que superan 5.0, en orden") {
    assert(fuertes(registro, 5.0) == List("Santander", "Choco", "Valle"))
  }

  test("fuertes: con un umbral alto no queda ninguno") {
    assert(fuertes(registro, 9.0) == List())
  }

  test("fuertes: conserva los repetidos") {
    assert(fuertes(registro, 3.0).length == 7)
  }

  test("fuertes: el umbral es estricto") {
    // Hay un sismo de magnitud 6.0 exacta; con umbral 6.0 no debe salir.
    assert(!fuertes(registro, 6.0).contains("Choco"))
  }

  test("enRegion: las magnitudes del Valle") {
    assert(enRegion(registro, "Valle") == List(4.2, 3.8, 5.3))
  }

  test("enRegion: una región que no está da la lista vacía") {
    assert(enRegion(registro, "Amazonas") == List())
  }

  test("enRegion: sobre el registro vacío no hay magnitudes") {
    assert(enRegion(vacio, "Valle") == List())
  }

  test("paresCercanos: con 0.6 solo Valle y Santander") {
    assert(paresCercanos(registro, 0.6) == List(("Valle", "Santander")))
  }

  test("paresCercanos: con 1.0 salen las tres parejas, en orden de aparición") {
    assert(paresCercanos(registro, 1.0) ==
      List(("Valle", "Santander"), ("Valle", "Choco"), ("Santander", "Choco")))
  }

  test("paresCercanos: con 0.1 no hay ninguna") {
    assert(paresCercanos(registro, 0.1) == List())
  }

  test("paresCercanos: cada pareja sale una sola vez") {
    val ps = paresCercanos(registro, 1.0)
    assert(ps.map { case (a, b) => Set(a, b) }.distinct.length == ps.length)
  }

  test("paresCercanos: nunca empareja una región consigo misma") {
    assert(paresCercanos(registro, 5.0).forall { case (a, b) => a != b })
  }

  // Punto 2 -------------------------------------------------------------------

  test("porRegion: agrupa los siete sismos en tres regiones") {
    assert(porRegion(registro).keySet == Set("Valle", "Santander", "Choco"))
    assert(porRegion(registro).values.map(_.length).sum == 7)
  }

  test("conteoPorRegion: tres, dos y dos") {
    assert(conteoPorRegion(registro) ==
      Map("Valle" -> 3, "Santander" -> 2, "Choco" -> 2))
  }

  test("magnitudMaxima: la mayor de cada región") {
    assert(magnitudMaxima(registro) ==
      Map("Valle" -> 5.3, "Santander" -> 5.1, "Choco" -> 6.0))
  }

  test("conteoPorRegion: el registro vacío da el mapa vacío") {
    assert(conteoPorRegion(vacio) == Map())
  }

  test("magnitudMaxima: las regiones ausentes no aparecen") {
    assert(!magnitudMaxima(registro).contains("Amazonas"))
  }

  // Punto 3 -------------------------------------------------------------------

  test("conteoSeguro: una región desconocida responde cero") {
    assert(conteoSeguro(registro)("Amazonas") == 0)
  }

  test("conteoSeguro: una región conocida responde su conteo") {
    assert(conteoSeguro(registro)("Valle") == 3)
  }

  test("conteoSeguro: sobre el registro vacío todo responde cero") {
    assert(conteoSeguro(vacio)("Valle") == 0)
  }

  test("maximaDe: el Chocó llega a 6.0") {
    assert(maximaDe(registro, "Choco") == Some(6.0))
  }

  test("maximaDe: una región sin registros da vacío") {
    assert(maximaDe(registro, "Amazonas") == None)
  }

  test("regionMasActiva: el Valle, con tres sismos") {
    assert(regionMasActiva(registro) == Some("Valle"))
  }

  test("regionMasActiva: sobre el registro vacío da vacío") {
    assert(regionMasActiva(vacio) == None)
  }

  test("regionMasActiva: en empate gana la primera alfabéticamente") {
    val empate = List(
      Sismo(Fecha(2025, 1), "Zulia", 4.0, 10),
      Sismo(Fecha(2025, 1), "Arauca", 4.0, 10))
    assert(regionMasActiva(empate) == Some("Arauca"))
  }

  // Punto 4 -------------------------------------------------------------------

  test("replicas: las primeras cuatro de un sismo de 5.0") {
    assert(replicas(5.0).take(4).toList == List(5.0, 4.0, 3.2, 2.56))
  }

  test("replicas: la secuencia es infinita y no se cuelga") {
    assert(replicas(5.0).take(200).toList.length == 200)
  }

  test("replicas: empieza en la magnitud dada") {
    assert(replicas(7.3).head == 7.3)
  }

  test("replicas: los valores van redondeados a dos decimales") {
    assert(replicas(5.0).take(6).toList.forall(x => x == Math.round(x * 100.0) / 100.0))
  }

  test("replicasSensibles: las que superan 3.0 partiendo de 5.0") {
    assert(replicasSensibles(5.0, 3.0) == List(5.0, 4.0, 3.2))
  }

  test("replicasSensibles: si la primera no supera el mínimo, no hay ninguna") {
    assert(replicasSensibles(2.0, 3.0) == List())
  }

  test("replicasSensibles: el mínimo es estricto") {
    assert(replicasSensibles(3.0, 3.0) == List())
  }

  // Punto 5 -------------------------------------------------------------------

  test("informe: el resumen de las tres regiones") {
    assert(informe(registro, 3.0) ==
      Map("Valle" -> Resumen(3, 5.3, 3),
        "Santander" -> Resumen(2, 5.1, 3),
        "Choco" -> Resumen(2, 6.0, 4)))
  }

  test("informe: el registro vacío da el mapa vacío") {
    assert(informe(vacio, 3.0) == Map())
  }

  test("informe: el conteo coincide con conteoPorRegion") {
    assert(informe(registro, 3.0).map { case (r, res) => (r, res.sismos) } ==
      conteoPorRegion(registro))
  }

  test("informe: la máxima coincide con magnitudMaxima") {
    assert(informe(registro, 3.0).map { case (r, res) => (r, res.maxima) } ==
      magnitudMaxima(registro))
  }

  test("informe: las réplicas coinciden con replicasSensibles del más fuerte") {
    assert(informe(registro, 3.0)("Choco").replicas ==
      replicasSensibles(6.0, 3.0).length)
  }





  //NUEVOS TEST


  // Punto 1
  test("fuertes: umbral bajo incluye todos los sismos del registro") {
    assert(fuertes(registro, 0.0).length == registro.length)
  }

  test("fuertes: registro vacío retorna lista vacía para cualquier umbral") {
    assert(fuertes(vacio, 2.0) == List())
  }

  test("enRegion: retorna las magnitudes respetando el orden original del registro") {
    val regCustom = List(Sismo(Fecha(2025, 1), "Antioquia", 4.5, 10), Sismo(Fecha(2025, 1), "Antioquia", 2.1, 10))
    assert(enRegion(regCustom, "Antioquia") == List(4.5, 2.1))
  }

  test("enRegion: región con un solo sismo retorna lista de un elemento") {
    val regCustom = List(Sismo(Fecha(2025, 1), "Bogota", 3.4, 5))
    assert(enRegion(regCustom, "Bogota") == List(3.4))
  }

  test("paresCercanos: con diferencia cero estricta si hay sismos idénticos") {
    val regCustom = List(Sismo(Fecha(2025, 1), "A", 4.0, 10), Sismo(Fecha(2025, 1), "B", 4.0, 10))
    assert(paresCercanos(regCustom, 0.0) == List(("A", "B")))
  }

  test("paresCercanos: sobre registro vacío retorna lista vacía") {
    assert(paresCercanos(vacio, 2.0) == List())
  }

  test("paresCercanos: orden de aparición en tuplas respeta el orden de las regiones") {
    val regCustom = List(Sismo(Fecha(2025, 1), "Zeta", 5.0, 10), Sismo(Fecha(2025, 1), "Alfa", 5.0, 10))
    assert(paresCercanos(regCustom, 0.0) == List(("Zeta", "Alfa")))
  }

  test("paresCercanos: umbral amplio detecta múltiples relaciones cruzadas") {
    assert(paresCercanos(registro, 5.0).length == 3)
  }

  // Punto 2
  test("porRegion: registro vacío produce mapa vacío") {
    assert(porRegion(vacio) == Map())
  }

  test("porRegion: cada lista agrupada contiene los sismos correctos de esa región") {
    val grupos = porRegion(registro)
    assert(grupos("Valle").forall(_.region == "Valle"))
    assert(grupos("Santander").forall(_.region == "Santander"))
  }

  test("conteoPorRegion: la suma de conteos es igual al tamaño total del registro") {
    val conteos = conteoPorRegion(registro)
    assert(conteos.values.sum == registro.length)
  }

  test("conteoPorRegion: registro con una sola región mapea un único elemento") {
    val regCustom = List(Sismo(Fecha(2025, 1), "Meta", 4.0, 10), Sismo(Fecha(2025, 1), "Meta", 3.0, 10))
    assert(conteoPorRegion(regCustom) == Map("Meta" -> 2))
  }

  test("magnitudMaxima: registro vacío retorna mapa vacío") {
    assert(magnitudMaxima(vacio) == Map())
  }

  test("magnitudMaxima: si todos tienen la misma magnitud, esa es la máxima") {
    val regCustom = List(Sismo(Fecha(2025, 1), "Tolima", 4.5, 10), Sismo(Fecha(2025, 1), "Tolima", 4.5, 12))
    assert(magnitudMaxima(regCustom) == Map("Tolima" -> 4.5))
  }

  test("porRegion: claves del mapa coinciden exactamente con las regiones distintas") {
    val regionesUnicas = registro.map(_.region).distinct.toSet
    assert(porRegion(registro).keySet == regionesUnicas)
  }

  test("conteoPorRegion: registro propio con dos regiones") {
    val propio = List(
      Sismo(Fecha(2025, 1), "A", 4.0, 10), Sismo(Fecha(2025, 1), "B", 4.0, 10),
      Sismo(Fecha(2025, 2), "A", 5.0, 10), Sismo(Fecha(2025, 3), "A", 6.0, 10),
      Sismo(Fecha(2025, 3), "B", 3.0, 10))
    assert(conteoPorRegion(propio) == Map("A" -> 3, "B" -> 2))
  }

  test("porRegion: los sismos del Valle quedan en el orden del registro") {
    assert(porRegion(registro)("Valle") == List(
      Sismo(Fecha(2025, 3), "Valle", 4.2, 30),
      Sismo(Fecha(2025, 4), "Valle", 3.8, 25),
      Sismo(Fecha(2025, 5), "Valle", 5.3, 35)))
  }

  // Punto 3
  test("conteoSeguro: multiples consultas a regiones inexistentes responden cero") {
    val seguro = conteoSeguro(registro)
    assert(seguro("Vichada") == 0)
    assert(seguro("Guainia") == 0)
  }

  test("conteoSeguro: sobre registro vacío multiples regiones responden cero") {
    val seguroVacio = conteoSeguro(vacio)
    assert(seguroVacio("Valle") == 0)
    assert(seguroVacio("Choco") == 0)
  }

  test("maximaDe: región existente conocida retorna Some con el valor correcto") {
    assert(maximaDe(registro, "Valle") == Some(5.3))
  }

  test("maximaDe: registro vacío devuelve None para cualquier región") {
    assert(maximaDe(vacio, "Valle") == None)
  }

  test("regionMasActiva: región única devuelve esa misma región") {
    val regCustom = List(Sismo(Fecha(2025, 1), "Nariño", 3.5, 10))
    assert(regionMasActiva(regCustom) == Some("Nariño"))
  }

  test("regionMasActiva: empate entre tres regiones selecciona la menor alfabéticamente") {
    val empateTres = List(
      Sismo(Fecha(2025, 1), "Caldas", 4.0, 10),
      Sismo(Fecha(2025, 1), "Antioquia", 4.0, 10),
      Sismo(Fecha(2025, 1), "Boyaca", 4.0, 10)
    )
    assert(regionMasActiva(empateTres) == Some("Antioquia"))
  }

  test("conteoSeguro: mapa por defecto funciona como Map normal para conocidas") {
    assert(conteoSeguro(registro)("Santander") == 2)
  }

  test("maximaDe: Santander retorna su máximo correcto") {
    assert(maximaDe(registro, "Santander") == Some(5.1))
  }

  // Punto 4
  test("replicas: primer elemento con magnitud decimal compleja se redondea bien") {
    assert(replicas(4.567).head == 4.57)
  }

  test("replicas: toma de elementos medianos mantiene la progresión multiplicativa") {
    val elems = replicas(10.0).take(3).toList
    assert(elems == List(10.0, 8.0, 6.4))
  }

  test("replicasSensibles: valor ligeramente superior al mínimo incluye el elemento") {
    assert(replicasSensibles(5.0, 3.99) == List(5.0, 4.0))
  }

  test("replicasSensibles: magnitud inicial menor al mínimo retorna lista vacía") {
    assert(replicasSensibles(1.0, 1.5) == List())
  }

  test("replicasSensibles: elementos devueltos cumplen estrictamente la condición de ser mayores al mínimo") {
    val res = replicasSensibles(6.0, 2.5)
    assert(res.forall(_ > 2.5))
  }

  test("replicas: las primeras cinco de un sismo de 10.0") {
    assert(replicas(10.0).take(5).toList == List(10.0, 8.0, 6.4, 5.12, 4.1))
  }

  test("replicas: lejos en la secuencia ya se llega a 0.0 sin colgarse") {
    assert(replicas(5.0).drop(1000).take(2).toList == List(0.0, 0.0))
  }

  test("replicasSensibles: 4.0 con mínimo 2.0 da cuatro réplicas") {
    assert(replicasSensibles(4.0, 2.0) == List(4.0, 3.2, 2.56, 2.05))
  }

  // Punto 5
  test("informe: registro vacío con cualquier umbral retorna mapa vacío") {
    assert(informe(vacio, 4.0) == Map())
  }

  test("informe: claves del mapa de informe corresponden exactamente a las regiones") {
    assert(informe(registro, 3.0).keySet == porRegion(registro).keySet)
  }

  test("informe: validación cruzada de máximas con funcion magnitudMaxima") {
    val inf = informe(registro, 2.0)
    val maxs = magnitudMaxima(registro)
    assert(inf.forall { case (reg, res) => res.maxima == maxs(reg) })
  }

  test("informe: validación cruzada de conteos con funcion conteoPorRegion") {
    val inf = informe(registro, 2.0)
    val conteos = conteoPorRegion(registro)
    assert(inf.forall { case (reg, res) => res.sismos == conteos(reg) })
  }

  test("informe: con umbral muy alto, las réplicas por región pueden reducirse a cero") {
    val inf = informe(registro, 10.0)
    assert(inf.values.forall(_.replicas == 0))
  }

  test("informe: estructura de Resumen se crea correctamente para una región simple") {
    val regCustom = List(Sismo(Fecha(2025, 1), "Cundinamarca", 4.0, 10))
    val inf = informe(regCustom, 2.0)
    assert(inf("Cundinamarca") == Resumen(1, 4.0, replicasSensibles(4.0, 2.0).length))
  }

  test("informe: la cantidad de entradas en el mapa de informe coincide con el total de regiones") {
    assert(informe(registro, 3.0).size == 3)
  }

  test("informe: con mínimo 4.0 cada región tiene 2 réplicas sensibles") {
    assert(informe(registro, 4.0) == Map(
      "Valle" -> Resumen(3, 5.3, 2),
      "Santander" -> Resumen(2, 5.1, 2),
      "Choco" -> Resumen(2, 6.0, 2)))
  }
}

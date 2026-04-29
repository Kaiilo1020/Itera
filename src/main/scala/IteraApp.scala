object IteraApp extends App {
  val nombre = if (args.nonEmpty) args.mkString(" ") else "mundo"
  println(s"Hola, $nombre. Bienvenido a Itera.")
}


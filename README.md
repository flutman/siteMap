# SiteMap

Программа для составления карты сайта.

### Возможности
  - Программа рекурсивно парсит ссылки сайта и формирует вывод ссылок в файл в папке data/
  - Для ускорения парсинга используется разбивка на несколько потоков с помощью ForkJoinPool
  - По окончании парсинга выводится в консоль время выполнения программы в секундах
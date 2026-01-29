# VEXO

Estoy desarrollando mi Trabajo Fin de Ciclo (TFC) del CFGS de Desarrollo de Aplicaciones Multiplataforma. El proyecto consiste en una aplicación móvil Android llamada Vexo, desarrollada en Android Studio con Kotlin, cuyo objetivo es ayudar a los usuarios a descubrir, organizar y decidir qué películas y series ver, reduciendo el tiempo de decisión y la saturación de contenido actual. 
La aplicación consumirá una API externa de cine y series (TMDB) para obtener información real y actualizada sobre películas y series, incluyendo contenido en tendencia, estrenos recientes, películas en cartelera, series populares y próximos lanzamientos. Vexo contará con varias funcionalidades principales: Recomendaciones de películas y series mediante dos métodos: 

Un sistema guiado por preguntas y filtros (género, duración, plataforma, popularidad, etc.). 
Un buscador libre donde el usuario pueda escribir frases como “una película corta y emocionante”. 
Sección de tendencias y novedades (lo más visto, estrenos y cartelera). 
Ficha detallada de cada película o serie. Sistema de historial donde el usuario pueda guardar el contenido que ya ha visto, valorarlo y escribir comentarios. 
Recomendaciones basadas en el historial del usuario. La aplicación seguirá una arquitectura MVVM, utilizando buenas prácticas de desarrollo móvil. La base del proyecto se centra en la experiencia de usuario y en el diseño centrado en el usuario (UX/UI). 

De forma opcional y como ampliación, se plantea integrar un componente de IA ligera, no generativa, orientada a mejorar las recomendaciones y la interpretación de búsquedas en lenguaje natural, sin intentar replicar sistemas complejos como los de Netflix. El proyecto está pensado para desarrollarse en varios meses y es totalmente factible a nivel académico, priorizando una implementación realista, funcional y bien documentada. Esto es el prompt que me has dado en otra conversación, ya tengo la API y tengo el repositorio del respectivo proyecto.

## STACK FINAL QUE VAS A USAR (decisión cerrada)

Para que no haya dudas ni cambios luego:

Lenguaje: Kotlin

UI: XML + ConstraintLayout (correcto para DAM)

Arquitectura: MVVM

Datos remotos: TMDB API

Datos locales: Room

Networking: Retrofit + Gson

Navegación: Activities + Intents (simple y defendible)

IA: opcional, al final

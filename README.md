<p align="center">
  <img src="screenshot/banner_vexo.PNG" width="900"/>
</p>

### Aplicación Android para descubrir, gestionar y decidir qué películas y series ver

VEXO es una aplicación móvil desarrollada para Android cuyo objetivo es ayudar al usuario a descubrir, organizar y gestionar contenido audiovisual de forma rápida, visual y personalizada.

La aplicación integra múltiples fuentes externas de información como **TMDB**, **OMDb** y **NewsAPI**, permitiendo centralizar datos sobre películas y series en una única plataforma.

---

# Descripción del proyecto

Actualmente existen numerosas plataformas de streaming y una enorme cantidad de contenido audiovisual disponible, lo que provoca que muchos usuarios:

- tarden demasiado tiempo en decidir qué ver,
- olviden películas o series ya vistas,
- no tengan una forma cómoda de organizar contenido,
- necesiten consultar múltiples aplicaciones para obtener información completa.

VEXO nace como una solución a este problema, ofreciendo:

- exploración de contenido,
- recomendaciones rápidas,
- organización mediante listas,
- historial de visualización,
- reseñas y valoraciones,
- noticias del sector,
- personalización de perfil,
- y un sistema de descubrimiento interactivo llamado **Discover**.

---

# Características principales

## Exploración de películas y series

La aplicación permite consultar:

- películas en tendencia,
- series populares,
- contenido mejor valorado,
- estrenos,
- recomendaciones relacionadas.

Incluye información detallada como:

- sinopsis,
- reparto,
- compañías productoras,
- trailers,
- plataformas de streaming,
- duración,
- temporadas,
- valoraciones externas,
- imágenes y recursos multimedia.

---

## Sistema de favoritos y listas

Los usuarios pueden:

- crear listas personalizadas,
- marcar contenido como favorito,
- registrar contenido visto,
- guardar películas descubiertas,
- compartir listas,
- gestionar listas públicas o privadas.

Además, la aplicación incorpora listas oficiales generadas por VEXO para facilitar el descubrimiento de contenido.

---

## Discover

Una de las funcionalidades más importantes del proyecto.

El sistema **Discover** ofrece una experiencia similar a Tinder:

- el usuario recibe recomendaciones individuales,
- puede aceptar o descartar contenido mediante swipe,
- y guardar automáticamente las recomendaciones aceptadas.

El sistema permite aplicar filtros avanzados:

- plataforma,
- género,
- duración,
- valoración,
- país,
- temática,
- año,
- tipo de contenido.

---

## Noticias del sector audiovisual

VEXO incorpora una sección de noticias actualizadas relacionadas con:

- películas,
- series,
- estrenos,
- actualidad del sector audiovisual.

Las noticias se obtienen mediante integración con **NewsAPI**.

---

## Perfil personalizable

Cada usuario dispone de:

- perfil propio,
- imagen personalizada,
- vitrina destacada,
- sistema de logros,
- historial,
- reseñas,
- diario de visualización,
- personalización visual del perfil.

La aplicación incorpora elementos de gamificación para aumentar la interacción del usuario.

---

# Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin | Desarrollo principal |
| Firebase Authentication | Gestión de usuarios |
| Cloud Firestore | Persistencia de datos |
| Retrofit | Consumo de APIs REST |
| Glide | Carga y caché de imágenes |
| RecyclerView | Listados dinámicos |
| ViewBinding | Gestión segura de vistas |
| TMDB API | Información audiovisual |
| OMDb API | Valoraciones externas |
| NewsAPI | Noticias del sector |
| Figma | Diseño de interfaz |

---

# Arquitectura

El proyecto sigue una arquitectura orientada a **MVVM (Model - View - ViewModel)**, utilizando repositorios para centralizar el acceso a datos y separar responsabilidades dentro de la aplicación.

```text
UI (Activities / XML)
        ↓
ViewModel
        ↓
Repository
        ↓
Firebase / APIs externas
(TMDB, OMDb, NewsAPI)
```

La aplicación utiliza:

- corrutinas de Kotlin,
- lifecycleScope,
- llamadas asíncronas,
- persistencia en Firestore,
- componentes Material Design.

---

# Backend y persistencia

VEXO utiliza Firebase para:

- autenticación,
- almacenamiento de datos,
- persistencia en la nube,
- sincronización entre dispositivos.

Cada usuario dispone de:

- listas,
- favoritos,
- contenido visto,
- reseñas,
- valoraciones,
- historial,
- vitrina personalizada.

---

# Landing Page oficial

VEXO dispone de una landing page propia desarrollada para presentar la aplicación, centralizar información del proyecto y permitir la descarga directa de la APK.

**Acceder a la landing page:**  
C:/Users/alejandro.delvalle/Desktop/LandingPage/index.html

## Login

<p align="center">
  <img src="screenshot/loggin.png" width="220"/>
  <img src="screenshot/loggin_v2.png" width="220"/>
</p>

Pantallas de acceso y registro de usuarios donde se permite iniciar sesión mediante correo electrónico y contraseña. También se incluye la posibilidad de crear una nueva cuenta directamente desde la aplicación.

---

## Menú principal

<p align="center">
  <img src="screenshot/home_menu_movies.png.jfif" width="220"/>
  <img src="screenshot/home_menu_series.jfif" width="220"/>
</p>

Pantallas principales de navegación donde se muestran películas y series destacadas organizadas por categorías. El usuario puede acceder rápidamente a contenido en tendencia, recomendaciones y apartados personalizados desde el menú inferior.

---

## Discover

<p align="center">
  <img src="screenshot/discover_filter.png.jfif" width="220"/>
  <img src="screenshot/discover_menu.png.jfif" width="220"/>
</p>

Sistema de descubrimiento basado en recomendaciones rápidas mediante swipe. El usuario puede aplicar filtros avanzados como género, plataforma, duración o valoración para obtener sugerencias más personalizadas.

---

## Ficha de películas

<p align="center">
  <img src="screenshot/movie_file.png.jfif" width="220"/><br><br>
  <img src="screenshot/movie_file_v4.jfif" width="220"/>
  <img src="screenshot/where_to_watch.jfif" width="220"/><br><br>
  <img src="screenshot/movie_file_v2.png.jfif" width="220"/>
  <img src="screenshot/profile_team.jfif" width="220"/>
  <img src="screenshot/profile_genres.jfif" width="220"/>
  <img src="screenshot/profile_detail.jfif" width="220"/><br><br>
  <img src="screenshot/movie_file_v3.png.jfif" width="220"/><br><br>
</p>

Vista detallada de películas donde se muestra información ampliada como sinopsis, plataformas disponibles, reparto, valoraciones, duración y recomendaciones relacionadas. También se incluye una pantalla específica para consultar dónde ver el contenido.

---

## Ficha de series

<p align="center">
  <img src="screenshot/serie_profile.png" width="220"/>
  <img src="screenshot/serie_profile_v2.png" width="220"/>
</p>

Pantallas dedicadas a series de televisión donde se pueden consultar temporadas, episodios, descripción completa y puntuaciones. Además, se muestran imágenes promocionales y contenido relacionado.

<p align="center">
  <img src="screenshot/feutures_series_movies_png.jfif" width="220"/>
</p>

Caracteristicas que tienen tanto las fichas de series como de peliculas, donde se puede valorarla nada más entrar o elegiir entre diferentes opciones como escribir una reseña, añadirla a tus listas, destacar en tu vitrina personal, marcar como vista o ver todos los posters de la misma.

<p align="center">
  <img src="screenshot/feutures_posters.jfif" width="220"/>
  <img src="screenshot/feutures_reviews.jfif" width="220"/>
</p>

Pantallas que comparten tanto las fichas de series como de peliculas, donde se pueden ver los posters y tanto valorar como escribir una reseña personal.

---

## Buscador y resultados

<p align="center">
  <img src="screenshot/seeker.png.jfif" width="220"/>
  <img src="screenshot/seeker_results_movies.png.jfif" width="220"/>
</p>

Motor de búsqueda integrado que permite localizar películas, series y actores. En el caso de los actores, se muestra una ficha específica con su información y contenidos relacionados.

<p align="center">
 <img src="screenshot/seeker_results_actors.png.jfif" width="220"/>
<img src="screenshot/profile_actor.jfif" width="220"/>
</p>

También, se puede buscar actores/actrices donde se enseñara información acerca de su vida como su filmografía.

---

## Sistema de listas


### Biblioteca y creación de listas

<p align="center">
  <img src="screenshot/library_lists.png.jfif" width="260"/>
  <img src="screenshot/new_lists.png.jfif" width="260"/>
</p>

Sistema de almacenamiento de listas personales y la posbilidad de crear las listas que quiera el usuario para guardar sus peliculas y series.


### Gestión de listas

<p align="center">
  <img src="screenshot/feutures_list.png.jfif" width="260"/>
</p>

Opciones avanzadas de gestión de listas, incluyendo configuración de privacidad, edición y acciones rápidas sobre colecciones creadas.

### Creación y exploración de listas

<p align="center">
  <img src="screenshot/new_lists.png.jfif" width="260"/>
</p>

Sistema de creación de nuevas colecciones personalizadas.

<p align="center">
  <img src="screenshot/other_lists.png.jfif" width="260"/>
  <img src="screenshot/other_lists_v2.jfif" width="260"/>
</p>

Colección de listas dentro de la plataforma para que el usuario tenga unas listas de referencias de contenido más común, como las mejores/series de la historia, el universo cinematografico de Marvel, Star Wars, todas las peliculas y series de Batman...

---

## Noticias audiovisuales

<p align="center">
  <img src="screenshot/movie_news.png.jfif" width="220"/>
  <img src="screenshot/movie_news_v2.jfif" width="220"/>
  <img src="screenshot/movie_news_v3.jfif" width="220"/>
</p>

Apartado de noticias donde se recopilan artículos relacionados con estrenos, un sistema para guardar noticias que quiera el usuario dentro de la propia aplicación y novedades del sector audiovisual utilizando información obtenida desde NewsAPI.

---

## Perfil de usuario

<p align="center">
  <img src="screenshot/profile_menu.png.jfif" width="220"/>
  <img src="screenshot/profile_menu_2.0.jfif" width="220"/><br><br>
  <img src="screenshot/profile_menu_v2.jfif" width="220"/><br><br>
  <img src="screenshot/profile_menu_v3.jfif" width="220"/><br><br>
  <img src="screenshot/profile_menu_v4.jfif" width="220"/>
</p>

Perfil personal del usuario donde se muestran estadísticas, actividad reciente, contenido guardado y accesos rápidos a funcionalidades como listas, logros, compartir su cuenta con sus contactos o configuración de cuenta.

## Diario y reseñas

<p align="center">
  <img src="screenshot/profile_diary.png.jfif" width="220"/>
  <img src="screenshot/profile_reviews_v2.png.jfif" width="220"/>
  <img src="screenshot/profile_reviews.png.jfif" width="220"/>
</p>

Sistema de diario personal y reseñas donde el usuario puede registrar películas y series vistas, añadir valoraciones y escribir opiniones sobre el contenido consumido.

---

## Gestión de perfil del usuario

<p align="center">
  <img src="screenshot/profile_managment.png.jfif" width="220"/>
</p>

Menú de gestión del perfil desde el que el usuario puede acceder a sus datos personales, configurar preferencias, consultar géneros destacados y visualizar información relacionada con el equipo o la identidad del proyecto.


## Personalización de perfil

<p align="center">
  <img src="screenshot/profile_personalize.png.jfif" width="220"/>
  <img src="screenshot/profile_personalize_v2.jfif" width="220"/>
</p>

Pantallas de configuración y personalización visual donde el usuario puede modificar los colores de cabecera, elegir si quiere la cabecera transparente o no y diferentes fondos tématicos de tanto el mundo del séptimo arte como otros tématicos, pudiendo elegir hasta 8 fondos originales para su perfil difente siendo: Un fondo original de Vexo, retrofuturista, del inmenso espacio, con instrumentos de una sala de cine, ambietado en un cine clásico, vaporwave, una playa paradiseaca y uno más urbano de callejones con graffitis de peliculas y series.

## Sistema de logros

<p align="center">
  <img src="screenshot/achievements.png.jfif" width="220"/>
  <img src="screenshot/achievements_v2.jfif" width="220"/>
</p>

Apartado de logros y progresión donde se desbloquean recompensas visuales según la actividad del usuario dentro de la aplicación, fomentando la interacción y la gamificación.

## Opciones del perfil de usuario

<p align="center">
  <img src="screenshot/profile_configuration.png.jfif" width="220"/>
  <img src="screenshot/profile_file.png.jfif" width="220"/>
</p>

Configuración del perfil del usuario donde puede ver los datos de su perfil, cambiar idioma entre el Español e Inglés y un apartado para averiguar un poco más sobre la pasión y el amor que le he puesto a este proyecto.

---

# Problemas encontrados durante el desarrollo

Durante el desarrollo aparecieron distintos retos técnicos:

| Problema | Solución |
|---|---|
| Gestión de múltiples APIs | Normalización de datos |
| Rendimiento de imágenes | Implementación de Glide |
| Gestión de asincronía | Corrutinas y lifecycleScope |
| Compatibilidad Android | Pruebas en distintos dispositivos |
| Gestión del ciclo de vida | Operaciones ligadas al lifecycle |

---

## Estimación de costes

| Concepto | Coste estimado |
|---|---|
| Desarrollo | 3.000 € – 4.000 € |
| Firebase | 0 € (plan gratuito) |
| APIs externas | 0 € |
| Cuenta Google Play | 25 € |
| Hosting Landing Page | 20 € – 60 € anuales |
| Mantenimiento | 15% – 20% anual |

---

# Futuras mejoras

El proyecto está planteado para evolucionar hacia una aplicación más completa.

Posibles mejoras futuras:

- arquitectura MVVM completa,
- inteligencia artificial para recomendaciones,
- notificaciones push,
- sistema social,
- rankings,
- comunidad,
- optimización offline,
- internacionalización,
- gamificación avanzada,
- publicación en Google Play.

---

# Viabilidad del proyecto

VEXO presenta:

- viabilidad técnica,
- bajo coste inicial,
- arquitectura escalable,
- potencial de crecimiento,
- y posibilidades reales de monetización.

El proyecto contempla modelos de:

- publicidad,
- personalización premium,
- afiliación con plataformas streaming,
- recomendaciones patrocinadas.

---

# Instalación

```bash
git clone https://github.com/BongoDeveloper26/VEXO
```

Abrir el proyecto con:

- Android Studio
- Kotlin
- SDK Android actualizado

Configurar:

- claves Firebase,
- API Keys de TMDB,
- OMDb,
- NewsAPI.

---

# Autor

**Alejandro del Valle López**  
Desarrollo de Aplicaciones Multiplataforma  
Trabajo Fin de Ciclo 2025/2026

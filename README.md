VEXO 
Aplicación Android para descubrir, gestionar y decidir qué ver en películas y series

VEXO es una aplicación móvil desarrollada para Android cuyo objetivo es ayudar al usuario a descubrir, organizar y gestionar contenido audiovisual de forma rápida, visual y personalizada.

La aplicación integra múltiples fuentes externas de información como TMDB, OMDb y NewsAPI, permitiendo centralizar datos sobre películas y series en una única plataforma.

Descripción del proyecto

Actualmente existen numerosas plataformas de streaming y una enorme cantidad de contenido audiovisual disponible, lo que provoca que muchos usuarios:

tarden demasiado tiempo en decidir qué ver,
olviden películas o series ya vistas,
no tengan una forma cómoda de organizar contenido,
necesiten consultar múltiples aplicaciones para obtener información completa.

VEXO nace como una solución a este problema, ofreciendo:

exploración de contenido,
recomendaciones rápidas,
organización mediante listas,
historial de visualización,
reseñas y valoraciones,
noticias del sector,
personalización de perfil,
y un sistema de descubrimiento interactivo llamado Discover.
Características principales
Exploración de películas y series

La aplicación permite consultar:

películas en tendencia,
series populares,
contenido mejor valorado,
estrenos,
recomendaciones relacionadas.

Incluye información detallada como:

sinopsis,
reparto,
compañías productoras,
trailers,
plataformas de streaming,
duración,
temporadas,
valoraciones externas,
imágenes y recursos multimedia.
Sistema de favoritos y listas

Los usuarios pueden:

crear listas personalizadas,
marcar contenido como favorito,
registrar contenido visto,
guardar películas descubiertas,
compartir listas,
gestionar listas públicas o privadas.

Además, la aplicación incorpora listas oficiales generadas por VEXO para facilitar el descubrimiento de contenido.

Modo Discover

Una de las funcionalidades más importantes del proyecto.

El sistema Discover ofrece una experiencia similar a Tinder:

el usuario recibe recomendaciones individuales,
puede aceptar o descartar contenido mediante swipe,
y guardar automáticamente las recomendaciones aceptadas.

El sistema permite aplicar filtros avanzados:

plataforma,
género,
duración,
valoración,
país,
temática,
año,
tipo de contenido.

Noticias del sector audiovisual

VEXO incorpora una sección de noticias actualizadas relacionadas con:

películas,
series,
estrenos,
actualidad del sector audiovisual.

Las noticias se obtienen mediante integración con NewsAPI.

Perfil personalizable

Cada usuario dispone de:

perfil propio,
imagen personalizada,
vitrina destacada,
sistema de logros,
historial,
reseñas,
diario de visualización,
personalización visual del perfil.

La aplicación incorpora elementos de gamificación para aumentar la interacción del usuario.

Tecnologías utilizadas
Tecnología	Uso
Kotlin	Desarrollo principal
Firebase Authentication	Gestión de usuarios
Cloud Firestore	Persistencia de datos
Retrofit	Consumo de APIs REST
Glide	Carga y caché de imágenes
RecyclerView	Listados dinámicos
ViewBinding	Gestión segura de vistas
TMDB API	Información audiovisual
OMDb API	Valoraciones externas
NewsAPI	Noticias del sector
Figma	Diseño de interfaz

Arquitectura

El proyecto sigue una arquitectura orientada a MVVM (Model - View - ViewModel), utilizando repositorios para centralizar el acceso a datos y separar responsabilidades dentro de la aplicación.

UI (Activities / XML)
        ↓
ViewModel
        ↓
Repository
        ↓
Firebase / APIs externas
(TMDB, OMDb, NewsAPI)

La aplicación utiliza:

corrutinas de Kotlin,
lifecycleScope,
llamadas asíncronas,
persistencia en Firestore,
componentes Material Design.

Backend y persistencia

VEXO utiliza Firebase para:

autenticación,
almacenamiento de datos,
persistencia en la nube,
sincronización entre dispositivos.

Cada usuario dispone de:

listas,
favoritos,
contenido visto,
reseñas,
valoraciones,
historial,
vitrina personalizada.

Capturas de la aplicación

Pantalla principal

Incluye contenido en tendencia y recomendaciones dinámicas.


Discover

Sistema de recomendación rápida mediante swipe.


Ficha de contenido

Pantalla detallada de películas y series.


Buscador

Búsqueda de películas, series y actores.


Perfil de usuario

Reseñas y diario

Sistema de logros

Sistema de listas

Noticias

Problemas encontrados durante el desarrollo

Durante el desarrollo aparecieron distintos retos técnicos:

Problema	Solución
Gestión de múltiples APIs	Normalización de datos
Rendimiento de imágenes	Implementación de Glide
Gestión de asincronía	Corrutinas y lifecycleScope
Compatibilidad Android	Pruebas en distintos dispositivos
Gestión del ciclo de vida	Operaciones ligadas al lifecycle

VEXO presenta:

viabilidad técnica,
bajo coste inicial,
arquitectura escalable,
potencial de crecimiento,
y posibilidades reales de monetización.

El proyecto contempla modelos de:

publicidad,
personalización premium,
afiliación con plataformas streaming,
recomendaciones patrocinadas.

Instalación
git clone https://github.com/BongoDeveloper26/VEXO

Abrir el proyecto con:

Android Studio
Kotlin
SDK Android actualizado

Configurar:

claves Firebase,
API Keys de TMDB,
OMDb,
NewsAPI.
Repositorio
https://github.com/BongoDeveloper26/VEXO

Autor

Alejandro del Valle López
Desarrollo de Aplicaciones Multiplataforma
Trabajo Fin de Ciclo 2025/2026

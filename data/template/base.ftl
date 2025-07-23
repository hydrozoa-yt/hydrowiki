<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="/files/lib/bootstrap/bootstrap.min.css" rel="stylesheet">
    <script src="/files/lib/bootstrap/bootstrap.bundle.min.js"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css">

    <title>${title}</title>
</head>
<body>
    <!-- Navigation bar -->
    <nav class="navbar navbar-expand-md bg-dark border-bottom border-body py-3 mb-5" data-bs-theme="dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="/">HydroWiki</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="/">Home</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/recent-changes/">Recent changes</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/all/">All articles</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/random/">Random page</a>
                    </li>
                    <#if loggedIn>
                        <li class="nav-item">
                        <a class="nav-link" href="/new/">New article</a>
                    </li>
                    </#if>
                    <li class="nav-item">
                        <#if loggedIn>
                            <a class="nav-link" href="/login?action=logout">Logout</a>
                        <#else>
                            <a class="nav-link" href="/login/">Login</a>
                        </#if>
                    </li>
                </ul>
                <form class="d-flex col-auto" role="search" action="/s">
                    <input class="form-control" type="search" name="terms" placeholder="Search" aria-label="Search">
                </form>
            </div>
        </div>
    </nav>

    <!-- Main content section -->
    <main>
        ${content}
    </main>

    <!-- Footer Section -->
    <footer class="footer bg-dark text-white py-4 mt-5 text-center">
        <div class="container">
            <div class="row">
                <div class="col-md-6 mb-3 mb-md-0">
                    <p class="mb-0">&copy; 2025 HydroWiki. Powered by &#10084;&#65039;.</p>
                </div>
                <div class="col-md-6">
                    <ul class="list-inline mb-0">
                        <li class="list-inline-item"><a href="/w/Invasion_of_the_heymen" class="text-white text-decoration-none">Link 1</a></li>
                        <li class="list-inline-item">&bull;</li>
                        <li class="list-inline-item"><a href="/w/Lesson_in_never_backing_down" class="text-white text-decoration-none">Link 2</a></li>
                    </ul>
                </div>
            </div>
        </div>
    </footer>
</body>
</html>
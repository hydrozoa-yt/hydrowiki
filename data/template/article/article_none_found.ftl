<div class="container">
    <div class="row align-items-center mb-3">
        <div class="col-md-9 border-bottom">
            <h1>No article found for "${articleNameHumanReadable}"</h1>
        </div>
    </div>
    <p>Maybe you want to author this article?</p>
    <#if isLoggedIn>
        <a href="/new?title=${articleName}" class="btn btn-primary d-inline-flex align-items-center">
            <i class="bi bi-plus-lg me-2"></i>
            Add article
        </a>
    <#else>
        <p>Log in to author the article.</p>
        <a href="/login/" class="btn btn-primary d-inline-flex align-items-center">
            Login
        </a>
    </#if>
</div>
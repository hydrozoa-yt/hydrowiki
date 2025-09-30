<div class="container">
    <div class="d-flex justify-content-end mb-4">
        <a href="?action=history" class="btn btn-primary rounded-element me-2">
            <i class="bi bi-clock-history me-2"></i>
            History
        </a>
        <#if loggedIn>
            <a href="?action=edit" class="btn btn-primary rounded-element me-2">
                <i class="bi bi-pencil-square me-2"></i>
                Edit page
            </a>
        </#if>
    </div>
<div>
<div class="container">
    <div class="row align-items-center mb-3">
        <div class="col-md-9 border-bottom">
            <h1>${articleNameHumanReadable}</h1>
            <#if articleAliasRedirect??>
                <p class="text-muted">Redirected from ${articleAliasRedirect}</p>
            </#if>
        </div>
    </div>
    ${articleContent}
</div>
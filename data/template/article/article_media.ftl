<div class="container">
    <div class="d-flex justify-content-end mb-4">
        <#if loggedIn>
            <a href="?action=edit" class="btn btn-primary rounded-element me-2">
                <i class="bi bi-pencil-square me-2"></i>
                Delete
            </a>
        </#if>
    </div>
<div>
<div class="container">
    <div class="row align-items-center mb-3">
        <div class="col-md-9 border-bottom">
            <h1>${articleName}</h1>
        </div>
    </div>
    <img src="${mediaUrl}" class="img-fluid">
    <p>Uploaded by ${user} on ${creation}</p>
</div>
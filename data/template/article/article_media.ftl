<div class="container">
    <div class="d-flex justify-content-end mb-4">
        <#if loggedIn>
            <a href="?action=delete" class="btn btn-danger rounded-element me-2" onclick="return confirm('Are you sure you want to delete this? This action cannot be undone.');">
                <i class="bi bi-trash3 me-2"></i>
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
    <p class="pt-3">Uploaded by ${user} on ${creation}</p>
</div>
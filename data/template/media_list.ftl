<div class="container">
    <#if infoMessage??>
        <#if infoMessage.type == "SUCCESS">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${infoMessage.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <#elseif infoMessage.type == "ERROR">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${infoMessage.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <#elseif infoMessage.type == "WARNING">
            <div class="alert alert-warning alert-dismissible fade show" role="alert">
                ${infoMessage.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </#if>
    </#if>

    <h1 class="mb-3 border-bottom">Media</h1>

    <div class="mb-5">
        <h2 class="mb-3">Upload a new image</h2>
        <form action="" method="post" enctype="multipart/form-data">
            <div class="mb-3">
                <label for="imageUpload" class="form-label">Choose an image file</label>
                <input class="form-control" type="file" id="imageUpload" name="imageUpload" accept="image/*" required>
            </div>
            <button type="submit" class="btn btn-primary">Upload Image</button>
        </form>
    </div>

    <hr>

    <div class="mt-5">
        <h2 class="mb-4">Gallery</h2>
        <#if medias?? && medias?size gt 0>
            <div class="row">
                <#list medias as media>
                    <div class="col-lg-4 col-md-6 mb-4">
                        <a href="/w/media:${media.filename}">
                            <img src="${media.url}" class="img-fluid rounded shadow-sm">
                        </a>
                    </div>
                </#list>
            </div>
        <#else>
            <p>No media found.</p>
        </#if>
    </div>
</div>
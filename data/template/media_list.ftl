<div class="container">
    <#if infoMessage?? && infoMessage?has_content>
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${infoMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
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
        <div class="row">
            <#list medias as media>
                <div class="col-lg-4 col-md-6 mb-4">
                    <a href="/w/media:${media.filename}">
                        <img src="${media.url}" class="img-fluid rounded shadow-sm">
                    </a>
                </div>
            </#list>
            <div class="col-lg-4 col-md-6 mb-4">
                <img src="https://placehold.co/600x400/EEE/31343C" class="img-fluid rounded shadow-sm" alt="Placeholder image">
            </div>
            <div class="col-lg-4 col-md-6 mb-4">
                <img src="https://placehold.co/600x400/5E6687/FFF" class="img-fluid rounded shadow-sm" alt="Placeholder image">
            </div>
            <div class="col-lg-4 col-md-6 mb-4">
                <img src="https://placehold.co/600x400/3E4357/FFF" class="img-fluid rounded shadow-sm" alt="Placeholder image">
            </div>
            <div class="col-lg-4 col-md-6 mb-4">
                <img src="https://placehold.co/600x400/9C91A5/FFF" class="img-fluid rounded shadow-sm" alt="Placeholder image">
            </div>
            <div class="col-lg-4 col-md-6 mb-4">
                <img src="https://placehold.co/600x400/292C37/FFF" class="img-fluid rounded shadow-sm" alt="Placeholder image">
            </div>
            <div class="col-lg-4 col-md-6 mb-4">
                <img src="https://placehold.co/600x400/D0D3DB/31343C" class="img-fluid rounded shadow-sm" alt="Placeholder image">
            </div>
        </div>
    </div>
</div>
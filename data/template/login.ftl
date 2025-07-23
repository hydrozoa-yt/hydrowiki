<div class="w-100 d-flex justify-content-center">

    <form method="post" style="width: 22rem;">

        <#if infoMessage?? && infoMessage?has_content>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${infoMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </#if>

        <h1 class="mb-5">Login</h1>
        <div class="mb-3">
            <label for="usernameInput" class="form-label">Username</label>
            <input
                type="text"
                name="usernameInput"
                id="usernameInput"
                placeholder="Enter your username"
                class="form-control"
                required
            >
        </div>

        <div class="mb-3">
            <label for="passwordInput" class="form-label">Password</label>
            <input
                type=password
                name="passwordInput"
                id="passwordInput"
                placeholder="Enter your password"
                class="form-control"
                required
            ></input>
        </div>

        <div class="d-grid mb-4 pt-4">
          <button type="submit" class="btn btn-primary btn-block mb-4">Login</button>
        </div>
    </form>
</div>
(function () {
  const BASE = typeof window.API_BASE === "string" ? window.API_BASE.replace(/\/$/, "") : "";

  const $ = (id) => document.getElementById(id);

  /** @type {number | null} */
  let currentRecipeId = null;

  function token() {
    return sessionStorage.getItem("token");
  }

  function setToken(t) {
    if (t) sessionStorage.setItem("token", t);
    else sessionStorage.removeItem("token");
    updateAuthUi();
  }

  function headers(json) {
    const h = {};
    if (json) h["Content-Type"] = "application/json";
    const t = token();
    if (t) h["Authorization"] = "Bearer " + t;
    return h;
  }

  async function api(path, opts) {
    const url = BASE + path;
    const res = await fetch(url, { ...opts, headers: { ...headers(!!opts.body), ...opts.headers } });
    const text = await res.text();
    let data;
    try {
      data = text ? JSON.parse(text) : null;
    } catch {
      data = text;
    }
    if (res.status === 204) return null;
    if (!res.ok) {
      const msg = typeof data === "object" && data && data.error ? data.error : text || res.statusText;
      throw new Error(msg + " (" + res.status + ")");
    }
    return data;
  }

  function setStatus(el, msg, ok) {
    el.textContent = msg || "";
    el.className = "status " + (ok ? "ok" : msg ? "err" : "");
  }

  function setFormMsg(msg, ok) {
    setStatus($("form-msg"), msg, ok);
  }

  function updateAuthUi() {
    const logged = !!token();
    $("btn-login").disabled = logged;
    $("btn-register").disabled = logged;
    $("btn-logout").disabled = !logged;
    $("recipe-section").style.opacity = logged ? "1" : "0.5";
    $("recipe-section").style.pointerEvents = logged ? "auto" : "none";
    if (logged) {
      loadList();
    } else {
      $("recipe-list").innerHTML = "";
      $("list-empty").classList.add("hidden");
      currentRecipeId = null;
      clearForm();
      updateEditMeta();
      $("btn-delete").disabled = true;
    }
  }

  function esc(s) {
    const d = document.createElement("div");
    d.textContent = s == null ? "" : String(s);
    return d.innerHTML;
  }

  function truncate(s, n) {
    if (s == null || s === "") return "—";
    const t = String(s).replace(/\s+/g, " ").trim();
    return t.length <= n ? t : t.slice(0, n) + "…";
  }

  async function loadList() {
    if (!token()) return;
    try {
      const items = await api("/api/recipes", { method: "GET" });
      renderList(Array.isArray(items) ? items : []);
    } catch (e) {
      $("recipe-list").innerHTML = "<p class=\"muted\">Не удалось загрузить список: " + esc(e.message) + "</p>";
      $("list-empty").classList.add("hidden");
    }
  }

  function renderList(items) {
    const wrap = $("recipe-list");
    const empty = $("list-empty");
    wrap.innerHTML = "";
    if (!items.length) {
      empty.classList.remove("hidden");
      return;
    }
    empty.classList.add("hidden");
    items.forEach((r) => {
      const el = document.createElement("article");
      el.className = "recipe-item" + (currentRecipeId === r.id ? " active" : "");
      el.innerHTML =
        "<div class=\"recipe-item-body\">" +
        "<strong class=\"recipe-title\">" +
        esc(r.title) +
        "</strong>" +
        "<span class=\"recipe-id\">№ " +
        r.id +
        "</span>" +
        "<p class=\"recipe-desc\">" +
        esc(truncate(r.description, 120)) +
        "</p>" +
        "</div>" +
        "<div class=\"recipe-item-actions\">" +
        "<button type=\"button\" class=\"small btn-open\" data-id=\"" +
        r.id +
        "\">Открыть</button>" +
        "<button type=\"button\" class=\"small secondary btn-del\" data-id=\"" +
        r.id +
        "\">Удалить</button>" +
        "</div>";
      el.querySelector(".btn-open").onclick = () => openRecipe(Number(r.id));
      el.querySelector(".btn-del").onclick = () => deleteRecipeFromList(Number(r.id));
      wrap.appendChild(el);
    });
  }

  function addIngredientRow(name, qty, unit) {
    const wrap = $("ingredients");
    const row = document.createElement("div");
    row.className = "ing-row";
    const inName = document.createElement("input");
    inName.type = "text";
    inName.placeholder = "Название";
    inName.className = "ing-name";
    inName.value = name != null ? String(name) : "";
    const inQty = document.createElement("input");
    inQty.type = "number";
    inQty.placeholder = "Кол-во";
    inQty.className = "ing-qty";
    inQty.step = "any";
    if (qty != null && qty !== "") inQty.value = String(qty);
    const inUnit = document.createElement("input");
    inUnit.type = "text";
    inUnit.placeholder = "Ед.";
    inUnit.className = "ing-unit";
    inUnit.value = unit != null ? String(unit) : "";
    const rm = document.createElement("button");
    rm.type = "button";
    rm.className = "remove secondary";
    rm.textContent = "✕";
    rm.onclick = () => row.remove();
    row.append(inName, inQty, inUnit, rm);
    wrap.appendChild(row);
  }

  function collectIngredients() {
    const rows = $("ingredients").querySelectorAll(".ing-row");
    const list = [];
    rows.forEach((r) => {
      const name = r.querySelector(".ing-name").value.trim();
      const qty = r.querySelector(".ing-qty").value;
      const unit = r.querySelector(".ing-unit").value.trim();
      if (!name && !qty && !unit) return;
      list.push({
        name: name || "?",
        quantity: qty === "" ? 1 : Number(qty),
        unit: unit || "шт",
      });
    });
    return list;
  }

  function clearForm() {
    $("r-title").value = "";
    $("r-desc").value = "";
    $("r-inst").value = "";
    $("ingredients").innerHTML = "";
    addIngredientRow("", "", "");
  }

  function fillForm(recipe) {
    $("r-title").value = recipe.title || "";
    $("r-desc").value = recipe.description || "";
    $("r-inst").value = recipe.instructions || "";
    $("ingredients").innerHTML = "";
    (recipe.ingredients || []).forEach((i) => addIngredientRow(i.name, i.quantity, i.unit));
    if ((recipe.ingredients || []).length === 0) addIngredientRow("", "", "");
  }

  function updateEditMeta() {
    const el = $("edit-meta");
    if (currentRecipeId == null) {
      el.textContent =
        "Новый рецепт.";
      $("btn-delete").disabled = true;
    } else {
      el.textContent = "Редактирование рецепта № " + currentRecipeId;
      $("btn-delete").disabled = false;
    }
  }

  function payloadFromForm() {
    return {
      title: $("r-title").value.trim() || "Без названия",
      description: $("r-desc").value,
      instructions: $("r-inst").value,
      ingredients: collectIngredients(),
    };
  }

  async function openRecipe(id) {
    if (!token()) return;
    setFormMsg("", true);
    try {
      const data = await api("/api/recipes/" + id, { method: "GET" });
      currentRecipeId = id;
      fillForm(data);
      updateEditMeta();
      await loadList();
    } catch (e) {
      setFormMsg(e.message, false);
    }
  }

  async function deleteRecipeFromList(id) {
    if (!token() || !confirm("Удалить рецепт № " + id + "?")) return;
    setFormMsg("", true);
    try {
      await api("/api/recipes/" + id, { method: "DELETE" });
      if (currentRecipeId === id) {
        currentRecipeId = null;
        clearForm();
        updateEditMeta();
      }
      setFormMsg("Рецепт № " + id + " удалён.", true);
      await loadList();
    } catch (e) {
      setFormMsg(e.message, false);
    }
  }

  $("btn-add-ing").onclick = () => addIngredientRow("", "", "");

  $("btn-new").onclick = () => {
    currentRecipeId = null;
    clearForm();
    updateEditMeta();
    setFormMsg("", true);
    loadList();
  };

  $("btn-refresh").onclick = () => loadList();

  $("btn-save").onclick = async () => {
    if (!token()) return;
    setFormMsg("", true);
    try {
      const body = JSON.stringify(payloadFromForm());
      if (currentRecipeId == null) {
        const data = await api("/api/recipes", { method: "POST", body });
        currentRecipeId = data.id;
        fillForm(data);
        updateEditMeta();
        setFormMsg("Создан рецепт № " + data.id + ".", true);
      } else {
        const data = await api("/api/recipes/" + currentRecipeId, { method: "PUT", body });
        fillForm(data);
        setFormMsg("Изменения сохранены.", true);
      }
      await loadList();
    } catch (e) {
      setFormMsg(e.message, false);
    }
  };

  $("btn-delete").onclick = async () => {
    if (currentRecipeId == null || !token()) return;
    if (!confirm("Удалить текущий рецепт № " + currentRecipeId + "?")) return;
    try {
      await api("/api/recipes/" + currentRecipeId, { method: "DELETE" });
      setFormMsg("Удалено.", true);
      currentRecipeId = null;
      clearForm();
      updateEditMeta();
      await loadList();
    } catch (e) {
      setFormMsg(e.message, false);
    }
  };

  function usernameFromEmail(email) {
    const e = email.trim();
    const at = e.indexOf("@");
    let base = at > 0 ? e.slice(0, at) : e;
    base = base.replace(/[^a-zA-Z0-9_]/g, "_").replace(/_+/g, "_").replace(/^_|_$/g, "");
    if (!base) base = "user";
    return base.slice(0, 100);
  }

  $("btn-register").onclick = async () => {
    const st = $("auth-status");
    try {
      const email = $("email").value.trim();
      const body = {
        email,
        password: $("password").value,
        username: usernameFromEmail(email),
      };
      const data = await api("/api/auth/register", { method: "POST", body: JSON.stringify(body) });
      setToken(data.token);
      setStatus(st, "Вошли как " + data.email, true);
    } catch (e) {
      setStatus(st, e.message, false);
    }
  };

  $("btn-login").onclick = async () => {
    const st = $("auth-status");
    try {
      const body = {
        email: $("email").value.trim(),
        password: $("password").value,
      };
      const data = await api("/api/auth/login", { method: "POST", body: JSON.stringify(body) });
      setToken(data.token);
      setStatus(st, "Вошли: " + data.email, true);
    } catch (e) {
      setStatus(st, e.message, false);
    }
  };

  $("btn-logout").onclick = () => {
    setToken(null);
    setStatus($("auth-status"), "Вы вышли", true);
  };

  addIngredientRow("", "", "");
  updateEditMeta();
  updateAuthUi();
})();

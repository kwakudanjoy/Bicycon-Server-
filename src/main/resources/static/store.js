// ============================================================
// OMART — RETAILER STORE FRONT
// ============================================================

// ============================================================
// ELEMENT REFERENCES
// ============================================================

const Loading = document.querySelector("#loading-overlay");
const Main = document.querySelector(".main");
const Cart_Overlay = document.querySelector(".cart-overlay");
const Payment_Overlay = document.querySelector(".payment-overlay");
const NoProducts = document.querySelector(".no-found-products");
const OrderSuccess = document.querySelector(".success-overlay");

const Call = document.querySelector("#call");
const PhoneNumber = document.querySelector(".phone");

const Retailer_Profile_Pic =
    document.querySelector(".retailer-signature > img");

const Pop_Up_profile_pic =
    document.querySelector(".avatar-large");

const ExpandButton =
    document.querySelector(".expand-btn");

const RetailerProfileExpand =
    document.querySelector(".retailer-profile-expand");

const Search_Icon =
    document.querySelector(".fa-search");

const Search_Input =
    document.querySelector(".Search-input");

const Cartegories =
    document.querySelector(".cartegories");


// ============================================================
// CART ELEMENTS
// ============================================================

const CartCloseButton =
    Cart_Overlay?.querySelector(".cart-close-btn");

const CartRetailerImage =
    Cart_Overlay?.querySelector(".cart-retailer__image");

const CartProductImage =
    Cart_Overlay?.querySelector(".cart-product__image");

const CartProductName =
    Cart_Overlay?.querySelector(".cart-product__name");

const CartProductPrice =
    Cart_Overlay?.querySelector(".cart-product__price");

const CartProductDescription =
    Cart_Overlay?.querySelector(".cart-product__description");

const CartRetailerName =
    Cart_Overlay?.querySelector(".cart-retailer__name");

const CartRetailerEmail =
    Cart_Overlay?.querySelector(".cart-retailer__email");

const CartRetailerPhone =
    Cart_Overlay?.querySelector(".cart-retailer__phone");

const CartMinus =
    Cart_Overlay?.querySelector(".minus");

const CartPlus =
    Cart_Overlay?.querySelector(".plus");

const CartQuantity =
    Cart_Overlay?.querySelector(".qty-number");

const CartTotal =
    Cart_Overlay?.querySelector(".total-amount");

const CartBuyButton =
    Cart_Overlay?.querySelector(".cart-buy-btn");


// ============================================================
// PAYMENT ELEMENTS
// ============================================================

const CancelPurchase =
    Payment_Overlay?.querySelector(".cancel-purchase");

const CustomerNumberInput =
    Payment_Overlay?.querySelector(".customer-number-input");

const PurchaseButton =
    Payment_Overlay?.querySelector(".purchase-btn");


// ============================================================
// SUCCESS ELEMENTS
// ============================================================

const SuccessOrderId =
    OrderSuccess?.querySelector("#display-order-id");

const SuccessCloseButton =
    OrderSuccess?.querySelector(".success-close-btn");


// ============================================================
// TOAST
// ============================================================

const toast =
    document.querySelector(".toast");

const toastIcon =
    document.querySelector(".toast-icon > i");

const toastHeader =
    document.querySelector(".toast-content > h4");

const toastText =
    document.querySelector(".toast-text");


// ============================================================
// BACKEND
// ============================================================

const IPAddress = Backend_End_Point;


// ============================================================
// GLOBAL PRODUCT STATE
// ============================================================

let product_ID = null;

let selectedProduct = {
    id: null,
    name: "",
    price: 0,
    currency: "",
    description: "",
    image: ""
};

let productQuantity = 1;


// ============================================================
// FORMATTER
// ============================================================

const formatter = new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
});


// ============================================================
// LOADING
// ============================================================

function showLoading() {
    if (Loading) {
        Loading.style.display = "flex";
    }
}

function hideLoading() {
    if (Loading) {
        Loading.style.display = "none";
    }
}


// ============================================================
// TOAST
// ============================================================

function showToast(icon, header, text, iconColor) {

    if (!toast) return;

    if (toastIcon) {
        toastIcon.className = "";

        icon.split(" ").forEach(cls => {
            toastIcon.classList.add(cls);
        });

        toastIcon.style.color = iconColor;
    }

    if (toastHeader) {
        toastHeader.textContent = header;
    }

    if (toastText) {
        toastText.textContent = text;
    }

    toast.classList.remove("hide");

    setTimeout(() => {
        toast.classList.add("show");
    }, 100);

    setTimeout(() => {
        toast.classList.remove("show");
        toast.classList.add("hide");
    }, 3000);
}


// ============================================================
// PROFILE
// ============================================================

function setRetailerProfile() {

    if (!Retailer_Profile_Pic) return;

    const fallback =
        "https://cdn-icons-png.flaticon.com/512/149/149071.png";

    if (Profile) {

        const profileURL =
            `${IPAddress}/profile/${Profile}`;

        Retailer_Profile_Pic.src = profileURL;

        if (Pop_Up_profile_pic) {
            Pop_Up_profile_pic.src = profileURL;
        }

        Retailer_Profile_Pic.onerror = () => {
            Retailer_Profile_Pic.src = fallback;
        };

        if (Pop_Up_profile_pic) {
            Pop_Up_profile_pic.onerror = () => {
                Pop_Up_profile_pic.src = fallback;
            };
        }

    } else {

        Retailer_Profile_Pic.src = fallback;

        if (Pop_Up_profile_pic) {
            Pop_Up_profile_pic.src = fallback;
        }
    }
}


// ============================================================
// RETAILER DROPDOWN
// ============================================================

if (ExpandButton && RetailerProfileExpand) {

    ExpandButton.addEventListener("click", e => {

        e.stopPropagation();

        RetailerProfileExpand.classList.toggle("active");
    });

    document.addEventListener("click", e => {

        if (!RetailerProfileExpand.contains(e.target)) {
            RetailerProfileExpand.classList.remove("active");
        }

    });
}


// ============================================================
// TOAST INITIALIZATION
// ============================================================

document.addEventListener("DOMContentLoaded", () => {

    if (toast) {
        toast.classList.add("hide");
    }

});


// ============================================================
// SEARCH
// ============================================================

if (Search_Input) {
    Search_Input.style.display = "none";
}

if (Search_Icon) {

    Search_Icon.addEventListener("click", () => {

        const Search_Content =
            Search_Input.value.trim();

        if (Search_Input.style.display === "block") {

            if (Search_Content === "") {

                Search_Input.style.display = "none";

                if (Cartegories) {
                    Cartegories.style.display = "flex";
                }

            } else {

                Search(Search_Content);
            }

        } else {

            Search_Input.style.display = "block";

            if (Cartegories) {
                Cartegories.style.display = "none";
            }

            Search_Input.focus();
        }

    });
}


if (Search_Input) {

    Search_Input.addEventListener("keydown", event => {

        if (event.key !== "Enter") return;

        const Search_Content =
            Search_Input.value.trim();

        if (Search_Content !== "") {
            Search(Search_Content);
        }

    });

}


function Search(query) {

    console.log("Searching for:", query);

    // Add your search algorithm here.
}


// ============================================================
// RETAILER INFORMATION
// ============================================================

function getRetailerInformation() {

    const nameElement =
        document.querySelector(".name");

    const idElement =
        document.querySelector(".id");

    const emailElement =
        document.querySelector(".email");

    const phoneElement =
        document.querySelector(".phone");


    const retailerName =
        nameElement?.textContent.trim() || "";

    const retailerID =
        idElement?.textContent
            .replace("ID:", "")
            .replace("#", "")
            .trim() || "";

    const retailerEmail =
        emailElement?.textContent.trim() || "";

    const retailerPhone =
        phoneElement?.textContent.trim() || "";


    return {
        name: retailerName,
        id: retailerID,
        email: retailerEmail,
        phone: retailerPhone
    };
}


// ============================================================
// GET ALL PRODUCTS
// ============================================================

async function GettingAllProducts() {

    const retailer =
        getRetailerInformation();

    if (!retailer.id) {

        console.error(
            "Retailer ID could not be found."
        );

        return;
    }


    const Payload = {

        INSTRUCTION: "GET-MY-PRODUCTS",

        User_id: retailer.id
    };


    try {

        showLoading();

        const Account_Products =
            await fetchData(Payload);


        if (
            Array.isArray(Account_Products) &&
            Account_Products.length > 0
        ) {

            Main.innerHTML = "";

            const fragment =
                document.createDocumentFragment();


            Account_Products.forEach(product => {

                const ProductCard =
                    document.createElement("div");

                ProductCard.className =
                    "a-product";


                // ----------------------------------------
                // STORE PRODUCT DATA ON CARD
                // ----------------------------------------

                ProductCard.dataset.productId =
                    product.Id;

                ProductCard.dataset.price =
                    product.price;

                ProductCard.dataset.currency =
                    product.currencyCode;


                // ----------------------------------------
                // CARD HTML
                // ----------------------------------------

                ProductCard.innerHTML = `

                    <img
                        src="${IPAddress}/products/${product.Url}"
                        alt="${product.name || ""}"
                        class="prod-img"
                        loading="lazy"
                    >

                    <p class="a-prod-name">
                        ${product.name || ""}
                    </p>

                    <p class="a-prod-price">
                        ${product.currencyCode || ""}
                        ${formatter.format(Number(product.price) || 0)}
                    </p>

                    <p class="a-prod-description">
                        ${product.description || ""}
                    </p>

                    <div class="prouduct-cart-bottom">

                        <p class="posted-at">
                            Posted ${product.postedAt || ""}
                        </p>

                        <div
                            class="order"
                            role="button"
                            tabindex="0"
                            aria-label="Buy product"
                        >
                            <i class="fa-solid fa-shopping-cart"></i>
                        </div>

                    </div>
                `;


                fragment.appendChild(ProductCard);

            });


            Main.appendChild(fragment);

            if (NoProducts) {
                NoProducts.style.display = "none";
            }


        } else {

            Main.innerHTML = "";

            if (NoProducts) {
                NoProducts.style.display = "flex";
            }

        }


    } catch (error) {

        console.error(
            "Error loading products:",
            error
        );

        if (NoProducts) {
            NoProducts.style.display = "flex";
        }

        showToast(
            "fa-solid fa-exclamation",
            "Connection Error",
            "Could not load the retailer's products.",
            "red"
        );

    } finally {

        hideLoading();

    }
}


// ============================================================
// OPEN PRODUCT CART
// ============================================================

async function openProductCart(productCard) {

    if (!productCard) return;


    // ========================================================
    // PRODUCT INFORMATION
    // ========================================================

    const productId =
        productCard.dataset.productId;

    const productPrice =
        Number(productCard.dataset.price);

    const currency =
        productCard.dataset.currency || "";


    const productName =
        productCard
            .querySelector(".a-prod-name")
            ?.textContent
            .trim() || "";


    const productDescription =
        productCard
            .querySelector(".a-prod-description")
            ?.textContent
            .trim() || "";


    const productImage =
        productCard
            .querySelector(".prod-img")
            ?.src || "";


    if (!productId) {

        showToast(
            "fa-solid fa-exclamation",
            "Error",
            "Product ID could not be found.",
            "red"
        );

        return;
    }


    if (!Number.isFinite(productPrice)) {

        showToast(
            "fa-solid fa-exclamation",
            "Error",
            "Product price is invalid.",
            "red"
        );

        return;
    }


    // ========================================================
    // SAVE CURRENT PRODUCT
    // ========================================================

    product_ID = productId;

    selectedProduct = {

        id: productId,

        name: productName,

        price: productPrice,

        currency: currency,

        description: productDescription,

        image: productImage
    };


    // ========================================================
    // GET RETAILER
    // ========================================================

    const retailer =
        getRetailerInformation();


    // ========================================================
    // RESET QUANTITY
    // ========================================================

    productQuantity = 1;


    if (CartQuantity) {
        CartQuantity.textContent = "1";
    }


    // ========================================================
    // SET CART PRODUCT
    // ========================================================

    if (CartProductImage) {
        CartProductImage.src = productImage;
    }

    if (CartProductName) {
        CartProductName.textContent =
            productName;
    }

    if (CartProductPrice) {
        CartProductPrice.textContent =
            `${currency} ${formatter.format(productPrice)}`;
    }

    if (CartProductDescription) {
        CartProductDescription.textContent =
            productDescription;
    }


    // ========================================================
    // SET CART RETAILER
    // ========================================================

    if (CartRetailerImage) {

        if (Retailer_Profile_Pic?.src) {

            CartRetailerImage.src =
                Retailer_Profile_Pic.src;

        }

    }

    if (CartRetailerName) {
        CartRetailerName.textContent =
            retailer.name;
    }

    if (CartRetailerEmail) {
        CartRetailerEmail.textContent =
            retailer.email;
    }

    if (CartRetailerPhone) {
        CartRetailerPhone.textContent =
            retailer.phone;
    }


    // ========================================================
    // INITIAL TOTAL
    // ========================================================

    updateCartTotal();


    // ========================================================
    // CHECK PRODUCT AVAILABILITY
    // ========================================================

    const inflatePayload = {

        INSTRUCTION:
            "INFLATE-TRY-TO-BUY",

        productID:
            productId
    };


    try {

        showLoading();

        const inflateResult =
            await fetchData(inflatePayload);


        if (
            inflateResult &&
            inflateResult.status === "OK"
        ) {

            Cart_Overlay.style.display =
                "flex";

        } else {

            showToast(
                "fa-solid fa-exclamation",
                "Unavailable",
                "This product is currently unavailable.",
                "red"
            );
        }


    } catch (error) {

        console.error(
            "Inflate error:",
            error
        );

        showToast(
            "fa-solid fa-exclamation",
            "Error",
            "Sorry, an error occurred while checking this product.",
            "red"
        );

    } finally {

        hideLoading();
    }
}


// ============================================================
// UPDATE CART TOTAL
// ============================================================

function updateCartTotal() {

    const total =
        productQuantity *
        selectedProduct.price;


    if (CartTotal) {

        CartTotal.textContent =
            `${selectedProduct.currency} ${formatter.format(total)}`;
    }
}


// ============================================================
// CART BUTTONS
// ============================================================

if (CartCloseButton) {

    CartCloseButton.addEventListener(
        "click",
        () => {

            Cart_Overlay.style.display =
                "none";

        }
    );
}


if (CartMinus) {

    CartMinus.addEventListener(
        "click",
        () => {

            if (productQuantity <= 1) {
                return;
            }

            productQuantity--;

            if (CartQuantity) {
                CartQuantity.textContent =
                    productQuantity;
            }

            updateCartTotal();
        }
    );
}


if (CartPlus) {

    CartPlus.addEventListener(
        "click",
        () => {

            productQuantity++;

            if (CartQuantity) {
                CartQuantity.textContent =
                    productQuantity;
            }

            updateCartTotal();
        }
    );
}


// ============================================================
// CART BUY BUTTON
// ============================================================

if (CartBuyButton) {

    CartBuyButton.addEventListener(
        "click",
        () => {

            Cart_Overlay.style.display =
                "none";

            Payment_Overlay.style.display =
                "flex";

            if (CustomerNumberInput) {
                CustomerNumberInput.value = "";
            }

        }
    );
}


// ============================================================
// MAIN PRODUCT CLICK
// ============================================================
//
// IMPORTANT:
// Products are created dynamically inside Main.
// Therefore we use event delegation here.
// ============================================================

if (Main) {

    Main.addEventListener(
        "click",
        async event => {

            const orderButton =
                event.target.closest(".order");


            // User did not click the shopping cart
            if (!orderButton) {
                return;
            }


            const productCard =
                orderButton.closest(".a-product");


            if (!productCard) {

                console.error(
                    "Could not find product card."
                );

                return;
            }


            console.log(
                "Product order button clicked:",
                productCard.dataset.productId
            );


            await openProductCart(productCard);

        }
    );


    // Keyboard support
    Main.addEventListener(
        "keydown",
        async event => {

            if (
                event.key !== "Enter" &&
                event.key !== " "
            ) {
                return;
            }


            const orderButton =
                event.target.closest(".order");


            if (!orderButton) {
                return;
            }


            event.preventDefault();


            const productCard =
                orderButton.closest(".a-product");


            if (!productCard) {
                return;
            }


            await openProductCart(productCard);

        }
    );

}


// ============================================================
// PAYMENT CANCEL
// ============================================================

if (CancelPurchase) {

    CancelPurchase.addEventListener(
        "click",
        () => {

            Payment_Overlay.style.display =
                "none";

            Cart_Overlay.style.display =
                "flex";

        }
    );
}


// ============================================================
// VALIDATE PRICE
// ============================================================

function ValidatePrice(inputValue) {

    const MAX_INT =
        "999999999999999999";


    inputValue =
        String(inputValue)
            .trim();


    const numericValue =
        inputValue.replace(/[^\d.]/g, "");


    if (!numericValue) {

        showToast(
            "fa-solid fa-money-bill",
            "Total Purchase",
            "Invalid purchase amount.",
            "#e53935"
        );

        return false;
    }


    const parts =
        numericValue.split(".");


    const intPart =
        parts[0];


    if (intPart.length > MAX_INT.length) {

        showToast(
            "fa-solid fa-money-bill",
            "Total Purchase",
            "Your purchase amount is too large. Try reducing your purchase quantity.",
            "#e53935"
        );

        return false;
    }


    if (
        intPart.length === MAX_INT.length &&
        intPart > MAX_INT
    ) {

        showToast(
            "fa-solid fa-money-bill",
            "Total Purchase",
            "Your purchase amount is too large. Try reducing your purchase quantity.",
            "#e53935"
        );

        return false;
    }


    return true;
}


// ============================================================
// PLACE ORDER
// ============================================================

if (PurchaseButton) {

    PurchaseButton.addEventListener(
        "click",
        async () => {

            // --------------------------------------------
            // PHONE VALIDATION
            // --------------------------------------------

            if (
                typeof iti !== "undefined" &&
                !iti.isValidNumber()
            ) {

                showToast(
                    "fa-solid fa-phone",
                    "Invalid Number",
                    "The number you entered is invalid.",
                    "red"
                );

                return;
            }


            let Phone = "";


            if (typeof iti !== "undefined") {
                Phone = iti.getNumber();
            }


            if (!Phone) {

                showToast(
                    "fa-solid fa-phone",
                    "Phone Required",
                    "Please enter your phone number.",
                    "red"
                );

                return;
            }


            // --------------------------------------------
            // TOTAL
            // --------------------------------------------

            const totalAmount =
                productQuantity *
                selectedProduct.price;


            if (
                !ValidatePrice(
                    String(totalAmount)
                )
            ) {
                return;
            }


            // --------------------------------------------
            // PAYLOAD
            // --------------------------------------------

            const Payload = {

                INSTRUCTION:
                    "PLACE-ORDER",

                ProductId:
                    product_ID,

                Quantity:
                    productQuantity,

                CustomerPhone:
                    Phone,

                ProductName:
                    selectedProduct.name,

                ProductPrice:
                    selectedProduct.price
            };


            console.log(
                "ORDER PAYLOAD:",
                Payload
            );


            // --------------------------------------------
            // SEND ORDER
            // --------------------------------------------

            try {

                showLoading();


                const Result =
                    await fetchData(Payload);


                if (
                    Result &&
                    Result.status === "OK"
                ) {

                    Payment_Overlay.style.display =
                        "none";


                    if (SuccessOrderId) {

                        SuccessOrderId.textContent =
                            "#" + Result.orderID;
                    }


                    OrderSuccess.style.display =
                        "flex";


                } else {

                    showToast(
                        "fa-solid fa-exclamation",
                        "Order Failed",
                        "Your order could not be placed. Please try again.",
                        "red"
                    );
                }


            } catch (error) {

                console.error(
                    "Place order error:",
                    error
                );

                showToast(
                    "fa-solid fa-exclamation",
                    "Network Error",
                    "Could not place the order. Please check your internet connection.",
                    "red"
                );

            } finally {

                hideLoading();
            }

        }
    );
}


// ============================================================
// SUCCESS CLOSE
// ============================================================

if (SuccessCloseButton) {

    SuccessCloseButton.addEventListener(
        "click",
        () => {

            OrderSuccess.style.display =
                "none";

        }
    );
}


// ============================================================
// PHONE CALL
// ============================================================

if (Call) {

    Call.addEventListener(
        "click",
        () => {

            const phone =
                PhoneNumber?.textContent.trim();


            if (!phone) {
                return;
            }


            window.location.href =
                `tel:${phone}`;

        }
    );
}


// ============================================================
// FETCH DATA
// ============================================================

async function fetchData(payload) {

    try {

        const response =
            await fetch(
                `${IPAddress}/api/process`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(payload)
                }
            );


        if (!response.ok) {

            throw new Error(
                `Network Error: ${response.status}`
            );
        }


        return await response.json();


    } catch (error) {

        console.error(
            "Fetch error:",
            error
        );

        throw error;
    }
}


// ============================================================
// INTERNATIONAL PHONE INPUT
// ============================================================

const CustomerPhoneInput =
    Payment_Overlay?.querySelector(
        ".customer-number-input"
    );


let iti = null;


if (
    CustomerPhoneInput &&
    window.intlTelInput
) {

    iti =
        window.intlTelInput(
            CustomerPhoneInput,
            {
                initialCountry: "auto",

                geoIpLookup: function (callback) {

                    fetch(
                        "https://ipapi.co/json"
                    )
                        .then(response =>
                            response.json()
                        )
                        .then(data => {

                            callback(
                                data.country_code ||
                                "gh"
                            );

                        })
                        .catch(() => {

                            callback("gh");

                        });

                },

                separateDialCode: true,

                useFullscreenPopup: false,

                utilsScript:
                    "https://cdn.jsdelivr.net/npm/intl-tel-input@19.5.5/build/js/utils.js"
            }
        );
}


// ============================================================
// INITIALIZATION
// ============================================================

function initializeStore() {

    setRetailerProfile();

    GettingAllProducts();
}


if (document.readyState === "loading") {

    document.addEventListener(
        "DOMContentLoaded",
        initializeStore
    );

} else {

    initializeStore();

}
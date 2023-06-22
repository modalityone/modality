package one.modality.base.frontoffice.states;

public class AccountHomePM {
    public static String PERSONAL_INFORMATION_TITLE = "Personal information";
    public static String PERSONAL_INFORMATION_SUBTITLE = "Edit your personal information here";
    public static String PERSONAL_INFORMATION_SVG_PATH = "M6.5 6.5C5.60625 6.5 4.84115 6.18177 4.20469 5.54531C3.56823 4.90885 3.25 4.14375 3.25 3.25C3.25 2.35625 3.56823 1.59115 4.20469 0.954688C4.84115 0.318229 5.60625 0 6.5 0C7.39375 0 8.15885 0.318229 8.79531 0.954688C9.43177 1.59115 9.75 2.35625 9.75 3.25C9.75 4.14375 9.43177 4.90885 8.79531 5.54531C8.15885 6.18177 7.39375 6.5 6.5 6.5ZM0 13V10.725C0 10.2646 0.118625 9.84127 0.355875 9.45506C0.592584 9.0694 0.907292 8.775 1.3 8.57188C2.13958 8.15208 2.99271 7.8371 3.85938 7.62694C4.72604 7.41731 5.60625 7.3125 6.5 7.3125C7.39375 7.3125 8.27396 7.41731 9.14062 7.62694C10.0073 7.8371 10.8604 8.15208 11.7 8.57188C12.0927 8.775 12.4074 9.0694 12.6441 9.45506C12.8814 9.84127 13 10.2646 13 10.725V13H0Z";

    public static String FAMILY_FRIENDS_TITLE = "Family or friends";
    public static String FAMILY_FRIENDS_SUBTITLE = "Add your family or friends information here";
    public static String FAMILY_FRIENDS_SVG_PATH = "M7 12C7 12 6 12 6 11C6 10 7 7 11 7C15 7 16 10 16 11C16 12 15 12 15 12H7ZM11 6C11.7956 6 12.5587 5.68393 13.1213 5.12132C13.6839 4.55871 14 3.79565 14 3C14 2.20435 13.6839 1.44129 13.1213 0.87868C12.5587 0.316071 11.7956 0 11 0C10.2044 0 9.44129 0.316071 8.87868 0.87868C8.31607 1.44129 8 2.20435 8 3C8 3.79565 8.31607 4.55871 8.87868 5.12132C9.44129 5.68393 10.2044 6 11 6ZM5.216 12C5.06776 11.6878 4.99382 11.3455 5 11C5 9.645 5.68 8.25 6.936 7.28C6.30909 7.08684 5.65595 6.99237 5 7C1 7 0 10 0 11C0 12 1 12 1 12H5.216ZM4.5 6C5.16304 6 5.79893 5.73661 6.26777 5.26777C6.73661 4.79893 7 4.16304 7 3.5C7 2.83696 6.73661 2.20107 6.26777 1.73223C5.79893 1.26339 5.16304 1 4.5 1C3.83696 1 3.20107 1.26339 2.73223 1.73223C2.26339 2.20107 2 2.83696 2 3.5C2 4.16304 2.26339 4.79893 2.73223 5.26777C3.20107 5.73661 3.83696 6 4.5 6Z";

    public static String MESSAGES_TITLE = "Messages";
    public static String MESSAGES_SUBTITLE = "Support messages";
    public static String MESSAGES_SVG_PATH = "M9.38885e-07 3.996C-0.000368082 3.47081 0.108047 2.9507 0.319038 2.46546C0.530028 1.98023 0.83945 1.53939 1.22958 1.16821C1.61972 0.797022 2.0829 0.502776 2.59261 0.302318C3.10232 0.101861 3.64854 -0.000871342 4.2 5.56683e-06H9.8C12.1191 5.56683e-06 14 1.79667 14 3.996V12H4.2C1.8809 12 9.38885e-07 10.2033 9.38885e-07 8.004V3.996ZM8.4 5.33334V6.66667H9.8V5.33334H8.4ZM4.2 5.33334V6.66667H5.6V5.33334H4.2Z";

    public static String PAYMENT_TITLE = "Wallet/Payments";
    public static String PAYMENT_SUBTITLE = "Your credit card information and past payments";
    public static String PAYMENT_SVG_PATH = "M1.3 10C0.9425 10 0.636567 9.87771 0.3822 9.63313C0.1274 9.38813 0 9.09375 0 8.75V1.25C0 0.90625 0.1274 0.612083 0.3822 0.3675C0.636567 0.1225 0.9425 0 1.3 0H11.7C12.0575 0 12.3636 0.1225 12.6184 0.3675C12.8728 0.612083 13 0.90625 13 1.25V8.75C13 9.09375 12.8728 9.38813 12.6184 9.63313C12.3636 9.87771 12.0575 10 11.7 10H1.3ZM1.3 5H11.7V2.5H1.3V5Z";

    public static String SETTINGS_TITLE = "Settings";
    public static String SETTINGS_SUBTITLE = "";
    public static String SETTINGS_SVG_PATH = "M5.08458 14L4.80597 11.76C4.65506 11.7017 4.51297 11.6317 4.3797 11.55C4.24597 11.4683 4.11526 11.3808 3.98756 11.2875L1.91542 12.1625L0 8.8375L1.79353 7.4725C1.78192 7.39083 1.77612 7.31197 1.77612 7.2359V6.7634C1.77612 6.6878 1.78192 6.60917 1.79353 6.5275L0 5.1625L1.91542 1.8375L3.98756 2.7125C4.11526 2.61917 4.24876 2.53167 4.38806 2.45C4.52736 2.36833 4.66667 2.29833 4.80597 2.24L5.08458 0H8.91542L9.19403 2.24C9.34494 2.29833 9.48726 2.36833 9.621 2.45C9.75426 2.53167 9.88474 2.61917 10.0124 2.7125L12.0846 1.8375L14 5.1625L12.2065 6.5275C12.2181 6.60917 12.2239 6.6878 12.2239 6.7634V7.2359C12.2239 7.31197 12.2123 7.39083 12.1891 7.4725L13.9826 8.8375L12.0672 12.1625L10.0124 11.2875C9.88474 11.3808 9.75124 11.4683 9.61194 11.55C9.47264 11.6317 9.33333 11.7017 9.19403 11.76L8.91542 14H5.08458ZM7.03483 9.45C7.70813 9.45 8.28275 9.21083 8.75871 8.7325C9.23466 8.25417 9.47264 7.67667 9.47264 7C9.47264 6.32333 9.23466 5.74583 8.75871 5.2675C8.28275 4.78917 7.70813 4.55 7.03483 4.55C6.34992 4.55 5.77227 4.78917 5.30189 5.2675C4.83197 5.74583 4.59702 6.32333 4.59702 7C4.59702 7.67667 4.83197 8.25417 5.30189 8.7325C5.77227 9.21083 6.34992 9.45 7.03483 9.45Z";

    public static String HELP_TITLE = "Help";
    public static String HELP_SUBTITLE = "";
    public static String HELP_SVG_PATH = "M7 0C3.136 0 0 3.136 0 7C0 10.864 3.136 14 7 14C10.864 14 14 10.864 14 7C14 3.136 10.864 0 7 0ZM7.7 11.9H6.3V10.5H7.7V11.9ZM9.149 6.475L8.519 7.119C8.015 7.63 7.7 8.05 7.7 9.1H6.3V8.75C6.3 7.98 6.615 7.28 7.119 6.769L7.987 5.887C8.246 5.635 8.4 5.285 8.4 4.9C8.4 4.13 7.77 3.5 7 3.5C6.23 3.5 5.6 4.13 5.6 4.9H4.2C4.2 3.353 5.453 2.1 7 2.1C8.547 2.1 9.8 3.353 9.8 4.9C9.8 5.516 9.548 6.076 9.149 6.475Z";

    public static String LEGAL_TITLE = "Legal";
    public static String LEGAL_SUBTLE = "Privacy and legal information regarding the use of data";
    public static String LEGAL_SVG_PATH = "M8.48344 0.407838C7.93969 -0.135946 7.05844 -0.135946 6.51469 0.407838L0.407813 6.51603C-0.135937 7.05982 -0.135937 7.94018 0.407813 8.48303L6.51656 14.5922C7.06031 15.1359 7.94062 15.1359 8.48344 14.5922L14.5922 8.48303C15.1359 7.93925 15.1359 7.05888 14.5922 6.51603L8.48344 0.407838ZM7.49906 3.75023C8.00062 3.75023 8.39344 4.18339 8.34281 4.6831L8.01469 7.97112C8.00366 8.10029 7.94456 8.22062 7.84908 8.3083C7.7536 8.39599 7.62869 8.44464 7.49906 8.44464C7.36943 8.44464 7.24452 8.39599 7.14904 8.3083C7.05356 8.22062 6.99446 8.10029 6.98344 7.97112L6.65531 4.6831C6.64353 4.56519 6.65657 4.44612 6.6936 4.33355C6.73064 4.22099 6.79083 4.11743 6.87032 4.02954C6.94981 3.94166 7.04682 3.8714 7.15511 3.8233C7.2634 3.77519 7.38057 3.7503 7.49906 3.75023ZM7.50094 9.37559C7.74958 9.37559 7.98803 9.47436 8.16385 9.65019C8.33966 9.82602 8.43844 10.0645 8.43844 10.3131C8.43844 10.5618 8.33966 10.8003 8.16385 10.9761C7.98803 11.1519 7.74958 11.2507 7.50094 11.2507C7.2523 11.2507 7.01384 11.1519 6.83803 10.9761C6.66221 10.8003 6.56344 10.5618 6.56344 10.3131C6.56344 10.0645 6.66221 9.82602 6.83803 9.65019C7.01384 9.47436 7.2523 9.37559 7.50094 9.37559Z";
}
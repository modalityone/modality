package one.modality.hotel.backoffice.activities.reception.i18n;

/**
 * Internationalization keys for the Reception Dashboard.
 *
 * All labels and messages used in the dashboard should reference these keys
 * and use I18nControls for automatic language binding.
 *
 * @author David Hello
 * @author Claude Code
 */
public interface ReceptionI18nKeys {

    // ==========================================
    // Loading State
    // ==========================================
    String Loading = "Loading";

    // ==========================================
    // Dashboard & Header
    // ==========================================
    String Reception = "Reception";
    String DharmaHaven = "DharmaHaven";
    String SearchGuests = "SearchGuests";
    String AllGuests = "AllGuests";
    String IndependentStays = "IndependentStays";

    // ==========================================
    // Tab Labels
    // ==========================================
    String Arriving = "Arriving";
    String NoShows = "NoShows";
    String Departing = "Departing";
    String InHouse = "InHouse";
    String Unpaid = "Unpaid";
    String CheckedOut = "CheckedOut";
    String All = "All";

    // ==========================================
    // Table Header Labels
    // ==========================================
    String HeaderGuest = "HeaderGuest";
    String HeaderEvent = "HeaderEvent";
    String HeaderRoom = "HeaderRoom";
    String HeaderDates = "HeaderDates";
    String HeaderBalance = "HeaderBalance";
    String HeaderStatus = "HeaderStatus";

    // ==========================================
    // Stat Cards
    // ==========================================
    String ArrivingToday = "ArrivingToday";
    String NoShowsToday = "NoShowsToday";
    String DepartingToday = "DepartingToday";
    String CurrentlyInHouse = "CurrentlyInHouse";
    String UnpaidBalances = "UnpaidBalances";

    // ==========================================
    // Guest Status Badges
    // ==========================================
    String StatusExpected = "StatusExpected";
    String StatusCheckedIn = "StatusCheckedIn";
    String StatusCheckedOut = "StatusCheckedOut";
    String StatusNoShow = "StatusNoShow";
    String StatusPreBooked = "StatusPreBooked";
    String StatusCancelled = "StatusCancelled";
    String StatusIn = "StatusIn";
    String StatusOut = "StatusOut";
    String StatusOffsite = "StatusOffsite";
    String StatusDeparting = "StatusDeparting";
    String NoDeposit = "NoDeposit";

    // ==========================================
    // Guest Row Labels
    // ==========================================
    String Room = "Room";
    String Email = "Email";
    String Phone = "Phone";
    String Independent = "Independent";
    String Balance = "Balance";
    String Paid = "Paid";
    String Deposit = "Deposit";
    String Total = "Total";
    String FirstMeal = "FirstMeal";
    String LastMeal = "LastMeal";
    String Diet = "Diet";
    String Notes = "Notes";

    // ==========================================
    // Action Buttons
    // ==========================================
    String CheckIn = "CheckIn";
    String CheckOut = "CheckOut";
    String Confirm = "Confirm";
    String Cancel = "Cancel";
    String View = "View";
    String Edit = "Edit";
    String MarkArrived = "MarkArrived";
    String Reactivate = "Reactivate";
    String CollectPayment = "CollectPayment";
    String Print = "Print";
    String Clear = "Clear";
    String Close = "Close";
    String Save = "Save";
    String Send = "Send";
    String Add = "Add";

    // ==========================================
    // Quick Actions Menu
    // ==========================================
    String QuickActions = "QuickActions";
    String NewBooking = "NewBooking";
    String CheckAvailability = "CheckAvailability";
    String More = "More";
    String BookMeals = "BookMeals";
    String BookTeachings = "BookTeachings";
    String SendMessage = "SendMessage";
    String FindGuest = "FindGuest";

    // ==========================================
    // Cards - Cash Register
    // ==========================================
    String CashRegister = "CashRegister";
    String Open = "Open";
    String Closed = "Closed";
    String Cash = "Cash";
    String Card = "Card";
    String TotalToday = "TotalToday";
    String ViewDetails = "ViewDetails";
    String OpenRegister = "OpenRegister";
    String CloseRegister = "CloseRegister";
    String OpeningCash = "OpeningCash";
    String Transactions = "Transactions";

    // ==========================================
    // Cards - Shift Notes
    // ==========================================
    String ShiftNotes = "ShiftNotes";
    String AddNote = "AddNote";
    String ViewAll = "ViewAll";
    String NoNotes = "NoNotes";
    String YourName = "YourName";
    String NoteContent = "NoteContent";
    String New = "New";

    // ==========================================
    // Cards - Messages
    // ==========================================
    String Messages = "Messages";
    String NewMessage = "NewMessage";
    String NoMessages = "NoMessages";
    String Urgent = "Urgent";
    String Normal = "Normal";
    String Priority = "Priority";
    String Sent = "Sent";

    // ==========================================
    // Cards - Fire List
    // ==========================================
    String FireList = "FireList";
    String Day = "Day";
    String Night = "Night";
    String TotalGuests = "TotalGuests";
    String Residents = "Residents";
    String Guests = "Guests";

    // ==========================================
    // Cards - Room Occupancy
    // ==========================================
    String Rooms = "Rooms";
    String Available = "Available";
    String Occupied = "Occupied";
    String Beds = "Beds";
    String Single = "Single";
    String Double = "Double";
    String Shared = "Shared";
    String Dormitory = "Dormitory";

    // ==========================================
    // Departments
    // ==========================================
    String Household = "Household";
    String Maintenance = "Maintenance";
    String Accounting = "Accounting";
    String Treasurer = "Treasurer";

    // ==========================================
    // Modals - Check In
    // ==========================================
    String CheckInGuest = "CheckInGuest";
    String CheckInConfirmation = "CheckInConfirmation";
    String Guest = "Guest";
    String UnpaidBalanceWarning = "UnpaidBalanceWarning";
    String ExpectedDate = "ExpectedDate";
    String DepartureDate = "DepartureDate";
    String PaymentDue = "PaymentDue";
    String DepositPaid = "DepositPaid";
    String PaymentMethod = "PaymentMethod";
    String Amount = "Amount";

    // ==========================================
    // Modals - Check Out
    // ==========================================
    String CheckOutGuest = "CheckOutGuest";
    String CheckOutConfirmation = "CheckOutConfirmation";
    String NotifyHousekeeping = "NotifyHousekeeping";
    String HousekeepingNotes = "HousekeepingNotes";
    String UnpaidBalanceCheckoutWarning = "UnpaidBalanceCheckoutWarning";
    String NightsStayed = "NightsStayed";
    String AlreadyPaid = "AlreadyPaid";
    String CollectBeforeCheckout = "CollectBeforeCheckout";
    String NotifyHousehold = "NotifyHousehold";
    String OffsiteParticipant = "OffsiteParticipant";
    String Arrived = "Arrived";

    // ==========================================
    // Modals - Cancel
    // ==========================================
    String CancelBooking = "CancelBooking";
    String KeepBooking = "KeepBooking";
    String RefundDeposit = "RefundDeposit";
    String RefundDepositHelp = "RefundDepositHelp";

    // ==========================================
    // Modals - Payment
    // ==========================================
    String PaymentDetails = "PaymentDetails";
    String TotalAmount = "TotalAmount";
    String AmountPaid = "AmountPaid";
    String BalanceDue = "BalanceDue";
    String PaymentAmount = "PaymentAmount";
    String AmountReceived = "AmountReceived";
    String PaymentRecorded = "PaymentRecorded";

    // ==========================================
    // Modals - Message
    // ==========================================
    String SendTo = "SendTo";
    String SelectRoom = "SelectRoom";
    String SelectGuest = "SelectGuest";
    String NoRoom = "NoRoom";
    String MessageContent = "MessageContent";

    // ==========================================
    // Modals - New Booking
    // ==========================================
    String CreateBooking = "CreateBooking";
    String BookingDetails = "BookingDetails";
    String GuestName = "GuestName";
    String FirstName = "FirstName";
    String LastName = "LastName";
    String GuestEmail = "GuestEmail";
    String GuestPhone = "GuestPhone";
    String SelectEvent = "SelectEvent";
    String Event = "Event";
    String SelectRoomType = "SelectRoomType";
    String AssignRoom = "AssignRoom";
    String StayDates = "StayDates";
    String ArrivalDate = "ArrivalDate";
    String EstimatedPrice = "EstimatedPrice";
    String MealPlan = "MealPlan";
    String Breakfast = "Breakfast";
    String Lunch = "Lunch";
    String Dinner = "Dinner";

    // ==========================================
    // Modals - Availability
    // ==========================================
    String CheckAvailabilityTitle = "CheckAvailabilityTitle";
    String FromDate = "FromDate";
    String ToDate = "ToDate";
    String RoomType = "RoomType";
    String AvailableRooms = "AvailableRooms";
    String NoRoomsAvailable = "NoRoomsAvailable";

    // ==========================================
    // Modals - Meal Booking
    // ==========================================
    String BookMealsTitle = "BookMealsTitle";
    String SelectMeals = "SelectMeals";
    String NumberOfGuests = "NumberOfGuests";
    String GuestType = "GuestType";
    String Adult = "Adult";
    String Teen = "Teen";
    String Child = "Child";
    String Infant = "Infant";
    String Standard = "Standard";
    String Vegetarian = "Vegetarian";
    String Vegan = "Vegan";
    String WheatFree = "WheatFree";
    String TotalPrice = "TotalPrice";

    // ==========================================
    // Modals - Teaching Booking
    // ==========================================
    String BookTeachingsTitle = "BookTeachingsTitle";
    String SelectTeaching = "SelectTeaching";
    String SelectSessions = "SelectSessions";
    String Date = "Date";
    String MorningGP = "MorningGP";
    String EveningGP = "EveningGP";
    String Empowerment = "Empowerment";

    // ==========================================
    // Modals - Find Guest
    // ==========================================
    String FindGuestTitle = "FindGuestTitle";
    String SearchByName = "SearchByName";
    String SearchByEmail = "SearchByEmail";
    String SearchByPhone = "SearchByPhone";
    String SearchByRoom = "SearchByRoom";
    String NoGuestsFound = "NoGuestsFound";

    // ==========================================
    // Modals - Guest Details
    // ==========================================
    String GuestDetails = "GuestDetails";
    String BookingHistory = "BookingHistory";
    String ContactInfo = "ContactInfo";
    String StayInfo = "StayInfo";
    String PaymentHistory = "PaymentHistory";

    // ==========================================
    // Modals - Register Details
    // ==========================================
    String RegisterDetails = "RegisterDetails";
    String Time = "Time";
    String GuestNameCol = "GuestNameCol";
    String Method = "Method";
    String AmountCol = "AmountCol";

    // ==========================================
    // Empty States
    // ==========================================
    String NoArrivalsToday = "NoArrivalsToday";
    String NoArrivalsDesc = "NoArrivalsDesc";
    String NoNoShows = "NoNoShows";
    String NoNoShowsDesc = "NoNoShowsDesc";
    String NoDeparturesToday = "NoDeparturesToday";
    String NoDeparturesDesc = "NoDeparturesDesc";
    String NoGuestsInHouse = "NoGuestsInHouse";
    String NoGuestsInHouseDesc = "NoGuestsInHouseDesc";
    String NoCheckoutsToday = "NoCheckoutsToday";
    String NoCheckoutsDesc = "NoCheckoutsDesc";
    String NoActiveBookings = "NoActiveBookings";
    String NoActiveBookingsDesc = "NoActiveBookingsDesc";

    // ==========================================
    // Unpaid Tab Sections
    // ==========================================
    String UnpaidMealBookings = "UnpaidMealBookings";
    String UnpaidTeachingBookings = "UnpaidTeachingBookings";
    String UnpaidStayBookings = "UnpaidStayBookings";
    String CollectAll = "CollectAll";

    // ==========================================
    // Keyboard Shortcuts
    // ==========================================
    String KeyboardShortcuts = "KeyboardShortcuts";
    String Navigation = "Navigation";
    String SwitchTabs = "SwitchTabs";
    String CloseModal = "CloseModal";
    String Actions = "Actions";
    String BulkSelect = "BulkSelect";
    String ShowHelp = "ShowHelp";

    // ==========================================
    // Pagination
    // ==========================================
    String PerPage = "PerPage";
    String ShowingResults = "ShowingResults";
    String Page = "Page";
    String Of = "Of";

    // ==========================================
    // Bulk Actions
    // ==========================================
    String BulkMode = "BulkMode";
    String SelectAll = "SelectAll";
    String DeselectAll = "DeselectAll";
    String Selected = "Selected";
    String CheckInSelected = "CheckInSelected";
    String CheckOutSelected = "CheckOutSelected";

    // ==========================================
    // Undo Toast
    // ==========================================
    String ActionCompleted = "ActionCompleted";
    String Undo = "Undo";
    String GuestCheckedIn = "GuestCheckedIn";
    String GuestCheckedOut = "GuestCheckedOut";
    String BookingCancelled = "BookingCancelled";
    String PaymentRecordedToast = "PaymentRecordedToast";
    String MessageSent = "MessageSent";
    String NoteSaved = "NoteSaved";

    // ==========================================
    // Date/Time
    // ==========================================
    String Today = "Today";
    String Yesterday = "Yesterday";
    String Tomorrow = "Tomorrow";
    String Morning = "Morning";
    String Evening = "Evening";
    String Afternoon = "Afternoon";

    // ==========================================
    // Additional Modal Keys
    // ==========================================
    String PartialPaymentWarning = "PartialPaymentWarning";
    String ByEvent = "ByEvent";
    String ByDates = "ByDates";
    String DepositReceived = "DepositReceived";
    String SuggestedDeposit = "SuggestedDeposit";
    String BalanceDueOnArrival = "BalanceDueOnArrival";
    String IndependentStay = "IndependentStay";
    String PayFull = "PayFull";
    String Booker = "Booker";
    String ContactPerson = "ContactPerson";
    String AddGuest = "AddGuest";
    String RemoveGuest = "RemoveGuest";
    String PayNow = "PayNow";
    String BookMealsOnly = "BookMealsOnly";
    String BookAndPay = "BookAndPay";
    String TeachingType = "TeachingType";
    String RelatedToGuest = "RelatedToGuest";
    String GeneralMessage = "GeneralMessage";
    String RoomLocation = "RoomLocation";
    String StaySummary = "StaySummary";
    String Offsite = "Offsite";
    String ExceedsBalance = "ExceedsBalance";
    String AmountWillRemain = "AmountWillRemain";
}
